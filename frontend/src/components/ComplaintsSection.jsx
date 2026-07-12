import { useEffect, useState } from 'react';
import { Box, Paper, Typography, Button, TextField, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Select, MenuItem, InputLabel, FormControl, Grid } from '@mui/material';
import { Report as ReportIcon, CheckCircle as ResolveIcon } from '@mui/icons-material';
import * as complaintService from '../services/complaintService';

export default function ComplaintsSection({ role }) {
  const [complaints, setComplaints] = useState([]);
  const [loading, setLoading] = useState(true);

  // File complaint form states
  const [subject, setSubject] = useState('');
  const [description, setDescription] = useState('');

  // Admin filter states
  const [filterStatus, setFilterStatus] = useState('OPEN');

  useEffect(() => {
    loadComplaints();
  }, [filterStatus]);

  async function loadComplaints() {
    try {
      setLoading(true);
      if (role === 'ROLE_ADMIN') {
        const res = await complaintService.searchComplaints(filterStatus);
        setComplaints(res.content || []);
      } else {
        const res = await complaintService.getMyComplaints();
        setComplaints(res.content || []);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  }

  async function handleFileComplaint(e) {
    e.preventDefault();
    try {
      await complaintService.fileComplaint({
        subject,
        description,
        businessId: null,
        listingId: null
      });
      setSubject('');
      setDescription('');
      loadComplaints();
    } catch (err) {
      console.error(err);
      alert('Error submitting dispute: ' + (err.response?.data?.message || err.message));
    }
  }

  async function handleResolve(id, status) {
    try {
      await complaintService.updateComplaintStatus(id, status);
      loadComplaints();
    } catch (e) {
      console.error(e);
      alert('Error updating status: ' + (e.response?.data?.message || e.message));
    }
  }

  return (
    <Box>
      {role !== 'ROLE_ADMIN' && (
        <Paper sx={{ p: 3, mb: 4, backgroundColor: '#181d1c', border: '1px solid rgba(255,255,255,0.06)' }}>
          <Typography variant="h6" sx={{ fontWeight: 'bold', mb: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
            <ReportIcon color="error" /> File a Dispute / Complaint
          </Typography>
          <form onSubmit={handleFileComplaint}>
            <Grid container spacing={2}>
              <Grid item xs={12}>
                <TextField fullWidth label="Subject" required value={subject} onChange={(e) => setSubject(e.target.value)} size="small" />
              </Grid>
              <Grid item xs={12}>
                <TextField fullWidth label="Description" required multiline rows={3} value={description} onChange={(e) => setDescription(e.target.value)} size="small" />
              </Grid>
              <Grid item xs={12}>
                <Button type="submit" variant="contained" color="error">Submit Dispute</Button>
              </Grid>
            </Grid>
          </form>
        </Paper>
      )}

      {role === 'ROLE_ADMIN' && (
        <Box sx={{ mb: 3, display: 'flex', gap: 2, alignItems: 'center' }}>
          <Typography variant="h6" sx={{ fontWeight: 'bold' }}>All Disputes / Complaints</Typography>
          <FormControl size="small" sx={{ minWidth: 150 }}>
            <InputLabel id="complaint-status-filter-label">Filter Status</InputLabel>
            <Select labelId="complaint-status-filter-label" label="Filter Status" value={filterStatus} onChange={(e) => setFilterStatus(e.target.value)}>
              <MenuItem value="OPEN">Open</MenuItem>
              <MenuItem value="UNDER_REVIEW">Under Review</MenuItem>
              <MenuItem value="RESOLVED">Resolved</MenuItem>
              <MenuItem value="REJECTED">Rejected</MenuItem>
            </Select>
          </FormControl>
        </Box>
      )}

      <Typography variant="h6" sx={{ fontWeight: 'bold', mb: 2 }}>{role === 'ROLE_ADMIN' ? '' : 'My Filed Disputes'}</Typography>
      {loading ? (
        <Typography>Loading disputes...</Typography>
      ) : complaints.length === 0 ? (
        <Typography color="text.secondary">No disputes found.</Typography>
      ) : (
        <TableContainer component={Paper} sx={{ backgroundColor: '#181d1c', border: '1px solid rgba(255,255,255,0.06)' }}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell sx={{ color: '#f2b84b', fontWeight: 'bold' }}>Reporter</TableCell>
                <TableCell sx={{ color: '#f2b84b', fontWeight: 'bold' }}>Business / Listing</TableCell>
                <TableCell sx={{ color: '#f2b84b', fontWeight: 'bold' }}>Subject</TableCell>
                <TableCell sx={{ color: '#f2b84b', fontWeight: 'bold' }}>Description</TableCell>
                <TableCell sx={{ color: '#f2b84b', fontWeight: 'bold' }}>Status</TableCell>
                {role === 'ROLE_ADMIN' && <TableCell sx={{ color: '#f2b84b', fontWeight: 'bold' }}>Action</TableCell>}
              </TableRow>
            </TableHead>
            <TableBody>
              {complaints.map((c) => (
                <TableRow key={c.id}>
                  <TableCell>{c.reporterName}</TableCell>
                  <TableCell>
                    {c.businessName && `Biz: ${c.businessName}`}
                    {c.listingName && ` (Listing: ${c.listingName})`}
                    {!c.businessName && !c.listingName && 'General'}
                  </TableCell>
                  <TableCell sx={{ fontWeight: 'medium' }}>{c.subject}</TableCell>
                  <TableCell>{c.description}</TableCell>
                  <TableCell>
                    <Typography variant="caption" sx={{ px: 1, py: 0.5, borderRadius: 1, fontWeight: 'bold', display: 'inline-block', backgroundColor: c.status === 'OPEN' ? '#d32f2f' : c.status === 'RESOLVED' ? '#2e7d5b' : '#f2b84b' }}>
                      {c.status}
                    </Typography>
                  </TableCell>
                  {role === 'ROLE_ADMIN' && (
                    <TableCell>
                      {c.status !== 'RESOLVED' && c.status !== 'REJECTED' && (
                        <Box sx={{ display: 'flex', gap: 1 }}>
                          <Button size="small" variant="outlined" color="success" onClick={() => handleResolve(c.id, 'RESOLVED')}>Resolve</Button>
                          <Button size="small" variant="outlined" color="warning" onClick={() => handleResolve(c.id, 'UNDER_REVIEW')}>Review</Button>
                          <Button size="small" variant="outlined" color="error" onClick={() => handleResolve(c.id, 'REJECTED')}>Reject</Button>
                        </Box>
                      )}
                    </TableCell>
                  )}
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}
    </Box>
  );
}
