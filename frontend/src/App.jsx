import { CssBaseline, ThemeProvider, createTheme } from '@mui/material';
import { Route, Routes, Navigate } from 'react-router';
import { useAuth } from './contexts/AuthContext';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';

const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#2e7d5b',
    },
    secondary: {
      main: '#f2b84b',
    },
    background: {
      default: '#111414',
      paper: '#181d1c',
    },
  },
  shape: {
    borderRadius: 8,
  },
});

export default function App() {
  const { isAuthenticated } = useAuth();

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Routes>
        {isAuthenticated ? (
          <>
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </>
        ) : (
          <>
            <Route path="/login" element={<Login />} />
            <Route path="*" element={<Navigate to="/login" replace />} />
          </>
        )}
      </Routes>
    </ThemeProvider>
  );
}
