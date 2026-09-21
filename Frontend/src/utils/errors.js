export function getErrorMessage(err, fallback = 'Something went wrong.') {
  return err?.response?.data?.message || err?.response?.data?.title || err?.message || fallback
}