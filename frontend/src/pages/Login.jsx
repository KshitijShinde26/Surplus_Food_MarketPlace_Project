import { useState } from 'react';
import { Container, Paper, Box, Tabs, Tab, TextField, Button, Typography, Grid, Select, MenuItem, InputLabel, FormControl, InputAdornment, IconButton } from '@mui/material';
import { MyLocation as LocationIcon } from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';
import * as businessService from '../services/businessService';
import * as ngoService from '../services/ngoService';

export default function Login() {
  const { login, register } = useAuth();
  const [tabValue, setTabValue] = useState(0); // 0 = login, 1 = register
  const [role, setRole] = useState('ROLE_CONSUMER'); // Default role
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // Form states
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [phone, setPhone] = useState('');
  const [lat, setLat] = useState('');
  const [lng, setLng] = useState('');

  // Business specific fields
  const [businessName, setBusinessName] = useState('');
  const [businessType, setBusinessType] = useState('GROCERY_STORE');
  const [licenseNumber, setLicenseNumber] = useState('');
  const [addressLine, setAddressLine] = useState('');
  const [city, setCity] = useState('');
  const [state, setState] = useState('');
  const [postalCode, setPostalCode] = useState('');

  // NGO specific fields
  const [ngoName, setNgoName] = useState('');
  const [ngoReg, setNgoReg] = useState('');
  const [ngoAddress, setNgoAddress] = useState('');

  function handleGetLocation() {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          setLat(pos.coords.latitude.toFixed(6));
          setLng(pos.coords.longitude.toFixed(6));
        },
        (err) => {
          console.warn("Geolocation failed: ", err);
          setError("Failed to fetch coordinates from browser geolocation. Please input manually.");
        }
      );
    } else {
      setError("Geolocation is not supported by your browser.");
    }
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      if (tabValue === 0) {
        // Login flow
        await login({ email, password });
      } else {
        // Register flow
        const userPayload = {
          fullName,
          email,
          phone,
          password,
          role,
          latitude: lat ? parseFloat(lat) : null,
          longitude: lng ? parseFloat(lng) : null,
        };

        const res = await register(userPayload);
        const token = res.accessToken;

        // If Business Owner, also initialize business profile
        if (role === 'ROLE_BUSINESS_OWNER') {
          const bizPayload = {
            businessName,
            businessType,
            licenseNumber,
            addressLine,
            city,
            state,
            postalCode,
            latitude: lat ? parseFloat(lat) : 0,
            longitude: lng ? parseFloat(lng) : 0,
          };
          // Temporarily override localStorage token for service call
          localStorage.setItem('accessToken', token);
          await businessService.createBusinessProfile(bizPayload);
        }

        // If NGO, also initialize NGO profile
        if (role === 'ROLE_NGO') {
          const ngoPayload = {
            organizationName: ngoName,
            registrationNumber: ngoReg,
            addressLine: ngoAddress,
            latitude: lat ? parseFloat(lat) : 0,
            longitude: lng ? parseFloat(lng) : 0,
          };
          // Temporarily override localStorage token for service call
          localStorage.setItem('accessToken', token);
          await ngoService.createNgoProfile(ngoPayload);
        }

        // reload auth user state to capture the created profiles
        window.location.reload();
      }
    } catch (err) {
      console.error(err);
      if (err.response?.data?.fieldErrors && err.response.data.fieldErrors.length > 0) {
        const errorList = err.response.data.fieldErrors.map(fe => `${fe.field}: ${fe.message}`).join(', ');
        setError(`Validation failed: ${errorList}`);
      } else {
        setError(err.response?.data?.message || err.message || 'An error occurred. Please verify your details.');
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <Container maxWidth="sm" sx={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', py: 4 }}>
      <Paper elevation={16} sx={{ p: 4, width: '100%', borderRadius: 3, backgroundColor: 'rgba(24, 29, 28, 0.95)', border: '1px solid rgba(255,255,255,0.06)' }}>
        <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', mb: 3 }}>
          <Typography variant="h4" sx={{ fontWeight: 'bold', color: '#f2b84b', mb: 1 }}>
            ♻️ Surplus Food Tracker
          </Typography>
          <Typography variant="subtitle2" sx={{ color: 'rgba(255,255,255,0.5)', textAlign: 'center' }}>
            Reducing food wastage by connecting sellers, consumers, and NGOs in real-time.
          </Typography>
        </Box>

        <Tabs value={tabValue} onChange={(_, val) => setTabValue(val)} variant="fullWidth" sx={{ borderBottom: 1, borderColor: 'rgba(255,255,255,0.1)', mb: 3 }}>
          <Tab label="Log In" sx={{ fontWeight: 'bold' }} />
          <Tab label="Register" sx={{ fontWeight: 'bold' }} />
        </Tabs>

        {error && (
          <Box sx={{ bgcolor: 'rgba(211, 47, 47, 0.1)', border: '1px solid #d32f2f', p: 1.5, mb: 3, borderRadius: 1, color: '#f8d7da', fontSize: '0.875rem' }}>
            {error}
          </Box>
        )}

        <form onSubmit={handleSubmit}>
          <Grid container spacing={2}>
            {tabValue === 1 && (
              <>
                <Grid item xs={12}>
                  <TextField fullWidth label="Full Name" required value={fullName} onChange={(e) => setFullName(e.target.value)} size="small" />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField fullWidth label="Phone" value={phone} onChange={(e) => setPhone(e.target.value)} size="small" />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <FormControl fullWidth size="small">
                    <InputLabel id="role-label">Role</InputLabel>
                    <Select labelId="role-label" label="Role" value={role} onChange={(e) => setRole(e.target.value)}>
                      <MenuItem value="ROLE_CONSUMER">Consumer</MenuItem>
                      <MenuItem value="ROLE_BUSINESS_OWNER">Business Owner</MenuItem>
                      <MenuItem value="ROLE_NGO">NGO Representative</MenuItem>
                    </Select>
                  </FormControl>
                </Grid>
              </>
            )}

            <Grid item xs={12}>
              <TextField fullWidth label="Email Address" type="email" required value={email} onChange={(e) => setEmail(e.target.value)} size="small" />
            </Grid>
            <Grid item xs={12}>
              <TextField fullWidth label="Password" type="password" required value={password} onChange={(e) => setPassword(e.target.value)} size="small" />
            </Grid>

            {tabValue === 1 && (
              <>
                {/* Geolocation Coordinate Fetchers */}
                <Grid item xs={6}>
                  <TextField fullWidth label="Latitude" type="number" step="any" required value={lat} onChange={(e) => setLat(e.target.value)} size="small"
                    InputProps={{
                      endAdornment: (
                        <InputAdornment position="end">
                          <Tooltip title="Get current coordinates">
                            <IconButton onClick={handleGetLocation} size="small" color="primary">
                              <LocationIcon fontSize="small" />
                            </IconButton>
                          </Tooltip>
                        </InputAdornment>
                      )
                    }}
                  />
                </Grid>
                <Grid item xs={6}>
                  <TextField fullWidth label="Longitude" type="number" step="any" required value={lng} onChange={(e) => setLng(e.target.value)} size="small" />
                </Grid>

                {/* Business Fields */}
                {role === 'ROLE_BUSINESS_OWNER' && (
                  <>
                    <Grid item xs={12}>
                      <Divider sx={{ my: 1, borderColor: 'rgba(255,255,255,0.08)' }}>Business Information</Divider>
                    </Grid>
                    <Grid item xs={12} sm={8}>
                      <TextField fullWidth label="Business Name" required value={businessName} onChange={(e) => setBusinessName(e.target.value)} size="small" />
                    </Grid>
                    <Grid item xs={12} sm={4}>
                      <FormControl fullWidth size="small">
                        <InputLabel id="biz-type-label">Type</InputLabel>
                        <Select labelId="biz-type-label" label="Type" value={businessType} onChange={(e) => setBusinessType(e.target.value)}>
                          <MenuItem value="GROCERY_STORE">Grocery Store</MenuItem>
                          <MenuItem value="RESTAURANT">Restaurant</MenuItem>
                          <MenuItem value="HOTEL">Hotel</MenuItem>
                          <MenuItem value="BAKERY">Bakery</MenuItem>
                          <MenuItem value="CAFE">Cafe</MenuItem>
                          <MenuItem value="SUPERMARKET">Supermarket</MenuItem>
                        </Select>
                      </FormControl>
                    </Grid>
                    <Grid item xs={12}>
                      <TextField fullWidth label="License Number" value={licenseNumber} onChange={(e) => setLicenseNumber(e.target.value)} size="small" />
                    </Grid>
                    <Grid item xs={12}>
                      <TextField fullWidth label="Address Line" required value={addressLine} onChange={(e) => setAddressLine(e.target.value)} size="small" />
                    </Grid>
                    <Grid item xs={12} sm={5}>
                      <TextField fullWidth label="City" required value={city} onChange={(e) => setCity(e.target.value)} size="small" />
                    </Grid>
                    <Grid item xs={6} sm={4}>
                      <TextField fullWidth label="State" required value={state} onChange={(e) => setState(e.target.value)} size="small" />
                    </Grid>
                    <Grid item xs={6} sm={3}>
                      <TextField fullWidth label="Postal Code" required value={postalCode} onChange={(e) => setPostalCode(e.target.value)} size="small" />
                    </Grid>
                  </>
                )}

                {/* NGO Fields */}
                {role === 'ROLE_NGO' && (
                  <>
                    <Grid item xs={12}>
                      <Divider sx={{ my: 1, borderColor: 'rgba(255,255,255,0.08)' }}>NGO Profile</Divider>
                    </Grid>
                    <Grid item xs={12}>
                      <TextField fullWidth label="Organization Name" required value={ngoName} onChange={(e) => setNgoName(e.target.value)} size="small" />
                    </Grid>
                    <Grid item xs={12}>
                      <TextField fullWidth label="NGO Registration Number" required value={ngoReg} onChange={(e) => setNgoReg(e.target.value)} size="small" />
                    </Grid>
                    <Grid item xs={12}>
                      <TextField fullWidth label="Address Line" required value={ngoAddress} onChange={(e) => setNgoAddress(e.target.value)} size="small" />
                    </Grid>
                  </>
                )}
              </>
            )}

            <Grid item xs={12} sx={{ mt: 2 }}>
              <Button fullWidth type="submit" variant="contained" color="primary" disabled={loading} sx={{ py: 1.2, fontWeight: 'bold' }}>
                {loading ? 'Processing...' : tabValue === 0 ? 'Log In' : 'Register Account'}
              </Button>
            </Grid>
          </Grid>
        </form>
      </Paper>
    </Container>
  );
}

// Inline Divider import wrapper helper
function Divider({ children, sx }) {
  return (
    <Box sx={{ display: 'flex', alignItems: 'center', width: '100%', ...sx }}>
      <Box sx={{ flexGrow: 1, height: '1px', bgcolor: 'rgba(255,255,255,0.1)' }} />
      {children && <Typography variant="caption" sx={{ px: 1, color: 'rgba(255,255,255,0.4)', textTransform: 'uppercase', letterSpacing: 1 }}>{children}</Typography>}
      <Box sx={{ flexGrow: 1, height: '1px', bgcolor: 'rgba(255,255,255,0.1)' }} />
    </Box>
  );
}

function Tooltip({ children, title }) {
  return (
    <Box component="span" style={{ position: 'relative', display: 'inline-flex' }}>
      {children}
    </Box>
  );
}
