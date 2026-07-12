import { useEffect, useState } from 'react';
import { Box, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Button, Typography, CircularProgress, Chip } from '@mui/material';
import { Check as ApproveIcon } from '@mui/icons-material';
import * as businessService from '../services/businessService';
import * as ngoService from '../services/ngoService';

export default function VerificationSection() {
  const [businesses, setBusinesses] = useState([]);
  const [ngos, setNgos] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadPendingProfiles();
  }, []);

  async function loadPendingProfiles() {
    try {
      setLoading(true);
      // Fetching all business profiles
      const bizRes = await businessService.getBusinessProfileAdminList(0, 100);
      setBusinesses((bizRes.content || []).filter(b => !b.verified));

      // Fetching all NGO profiles
      const ngoRes = await ngoService.getNgoProfileAdminList(0, 100);
      setNgos((ngoRes.content || []).filter(n => !n.verified));
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  }

  async function handleVerifyBusiness(id) {
    try {
      await businessService.verifyBusinessProfile(id);
      setBusinesses(prev => prev.filter(b => b.id !== id));
    } catch (e) {
      console.error(e);
    }
  }

  async function handleVerifyNgo(id) {
    try {
      await ngoService.verifyNgoProfile(id);
      setNgos(prev => prev.filter(n => n.id !== id));
    } catch (e) {
      console.error(e);
    }
  }

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Box sx={{ mb: 4 }}>
        <Typography variant="h6" sx={{ fontWeight: 'bold', mb: 2 }}>Pending Businesses ({businesses.length})</Typography>
        {businesses.length === 0 ? (
          <Typography color="text.secondary">No pending business verifications.</Typography>
        ) : (
          <TableContainer component={Paper} sx={{ backgroundColor: '#181d1c', border: '1px solid rgba(255,255,255,0.06)' }}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell sx={{ color: '#f2b84b', fontWeight: 'bold' }}>Business Name</TableCell>
                  <TableCell sx={{ color: '#f2b84b', fontWeight: 'bold' }}>Type</TableCell>
                  <TableCell sx={{ color: '#f2b84b', fontWeight: 'bold' }}>License Number</TableCell>
                  <TableCell sx={{ color: '#f2b84b', fontWeight: 'bold' }}>Location</TableCell>
                  <TableCell sx={{ color: '#f2b84b', fontWeight: 'bold' }}>Action</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {businesses.map((b) => (
                  <TableRow key={b.id}>
                    <TableCell sx={{ fontWeight: 'medium' }}>{b.businessName}</TableCell>
                    <TableCell>
                      <Chip label={b.businessType} size="small" variant="outlined" />
                    </TableCell>
                    <TableCell>{b.licenseNumber || 'N/A'}</TableCell>
                    <TableCell>{b.city}, {b.state}</TableCell>
                    <TableCell>
                      <Button size="small" variant="contained" color="primary" startIcon={<ApproveIcon />} onClick={() => handleVerifyBusiness(b.id)}>
                        Verify
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Box>

      <Box>
        <Typography variant="h6" sx={{ fontWeight: 'bold', mb: 2 }}>Pending NGOs ({ngos.length})</Typography>
        {ngos.length === 0 ? (
          <Typography color="text.secondary">No pending NGO verifications.</Typography>
        ) : (
          <TableContainer component={Paper} sx={{ backgroundColor: '#181d1c', border: '1px solid rgba(255,255,255,0.06)' }}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell sx={{ color: '#f2b84b', fontWeight: 'bold' }}>Organization Name</TableCell>
                  <TableCell sx={{ color: '#f2b84b', fontWeight: 'bold' }}>Reg Number</TableCell>
                  <TableCell sx={{ color: '#f2b84b', fontWeight: 'bold' }}>Address</TableCell>
                  <TableCell sx={{ color: '#f2b84b', fontWeight: 'bold' }}>Action</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {ngos.map((n) => (
                  <TableRow key={n.id}>
                    <TableCell sx={{ fontWeight: 'medium' }}>{n.organizationName}</TableCell>
                    <TableCell>{n.registrationNumber}</TableCell>
                    <TableCell>{n.addressLine}</TableCell>
                    <TableCell>
                      <Button size="small" variant="contained" color="primary" startIcon={<ApproveIcon />} onClick={() => handleVerifyNgo(n.id)}>
                        Verify
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Box>
    </Box>
  );
}
