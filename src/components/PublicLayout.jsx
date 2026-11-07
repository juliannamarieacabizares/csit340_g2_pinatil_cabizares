import { Outlet } from 'react-router-dom';

export default function PublicLayout() {
  return (
    <div className="App">
      <Outlet />
    </div>
  );
}