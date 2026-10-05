// -----------------------------------------------------------------------------
// File: ProsumerDTOs.cs
// Author: Gunasena R. K. R. M. S. K.
// Purpose: System implementation for ProsumerDTOs
// -----------------------------------------------------------------------------

namespace SolarGrid.Api.Models;

public class CreateProsumerRequest
{
    public string Nic { get; set; } = null!;
    public string FullName { get; set; } = null!;
    public string Email { get; set; } = null!;
    public string Phone { get; set; } = null!;
    public string Address { get; set; } = null!;
    public string Password { get; set; } = null!;
}

public class UpdateProsumerRequest
{
    public string FullName { get; set; } = null!;
    public string Email { get; set; } = null!;
    public string Phone { get; set; } = null!;
    public string Address { get; set; } = null!;
}
