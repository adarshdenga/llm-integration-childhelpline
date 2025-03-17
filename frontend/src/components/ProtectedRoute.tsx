import { Navigate } from "react-router-dom";

export default function ProtectedRoute({ children }: any) {
  const sessionID = localStorage.getItem("bdi_session_id");
  if (!sessionID) {
    return <Navigate to="/" replace />;
  }
  return <>{children}</>;
}
