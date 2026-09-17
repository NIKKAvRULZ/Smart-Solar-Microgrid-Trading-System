package com.team.smartsolar.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.team.smartsolar.R;
import com.team.smartsolar.models.Booking;
import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private List<Booking> bookingList;

    public BookingAdapter(List<Booking> bookingList) {
        this.bookingList = bookingList;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);
        holder.txtItemStation.setText(booking.getStationName());
        holder.txtItemDateTime.setText(booking.getDate() + " | " + booking.getTime());
        holder.txtItemEnergy.setText("Reserved: " + booking.getEnergyAmount() + " kWh");
        holder.txtItemStatus.setText(booking.getStatus().toUpperCase());

        // Make the entire card clickable
        holder.itemView.setOnClickListener(v -> {
            if (booking.getStatus().equalsIgnoreCase("Approved")) {
                // Open the QR ticket
                android.content.Intent intent = new android.content.Intent(v.getContext(), com.team.smartsolar.ViewQrActivity.class);
                intent.putExtra("STATION", booking.getStationName());
                intent.putExtra("DATE", booking.getDate() + " " + booking.getTime());
                intent.putExtra("ENERGY", booking.getEnergyAmount());
                v.getContext().startActivity(intent);

            } else if (booking.getStatus().equalsIgnoreCase("Pending")) {
                // Open the Modify/Cancel screen
                android.content.Intent intent = new android.content.Intent(v.getContext(), com.team.smartsolar.ModifyBookingActivity.class);
                intent.putExtra("STATION", booking.getStationName());
                intent.putExtra("DATE", booking.getDate());
                intent.putExtra("TIME", booking.getTime());
                intent.putExtra("ENERGY", booking.getEnergyAmount());
                v.getContext().startActivity(intent);

            } else {
                android.widget.Toast.makeText(v.getContext(),
                        "This booking is already " + booking.getStatus(),
                        android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView txtItemStation, txtItemDateTime, txtItemEnergy, txtItemStatus;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            txtItemStation = itemView.findViewById(R.id.txtItemStation);
            txtItemDateTime = itemView.findViewById(R.id.txtItemDateTime);
            txtItemEnergy = itemView.findViewById(R.id.txtItemEnergy);
            txtItemStatus = itemView.findViewById(R.id.txtItemStatus);
        }
    }
}