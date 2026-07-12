import { useEffect, useState } from 'react';
import { Box, Grid, Card, CardContent, Typography, CircularProgress } from '@mui/material';
import { AccountBalanceWallet as RevenueIcon, VolunteerActivism as DonationIcon, RestoreFromTrash as WasteIcon, Star as StarIcon } from '@mui/icons-material';
import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend } from 'chart.js';
import { Bar } from 'react-chartjs-2';
import * as analyticsService from '../services/analyticsService';

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend);

export default function AnalyticsDashboard({ role }) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadAnalytics();
  }, [role]);

  async function loadAnalytics() {
    try {
      setLoading(true);
      let res;
      if (role === 'ROLE_BUSINESS_OWNER') {
        res = await analyticsService.getBusinessAnalytics();
      } else if (role === 'ROLE_ADMIN') {
        res = await analyticsService.getAdminAnalytics();
      } else if (role === 'ROLE_NGO') {
        res = await analyticsService.getNgoAnalytics();
      }
      setData(res);
    } catch (e) {
      console.error("Failed to load analytics: ", e);
    } finally {
      setLoading(false);
    }
  }

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (!data) {
    return (
      <Typography color="text.secondary" align="center">
        No analytics data available at this time.
      </Typography>
    );
  }

  // Formatting chart metrics
  const hasRevenueTrend = data.revenueTrend && data.revenueTrend.length > 0;
  const hasDonationTrend = data.donationTrend && data.donationTrend.length > 0;
  const hasAdminTrend = data.globalOrderTrend && data.globalOrderTrend.length > 0;
  const hasNgoTrend = data.claimsTrend && data.claimsTrend.length > 0;

  const chartData = {
    labels: hasRevenueTrend 
      ? data.revenueTrend.map(t => t.label)
      : hasAdminTrend 
        ? data.globalOrderTrend.map(t => t.label)
        : hasNgoTrend 
          ? data.claimsTrend.map(t => t.label)
          : ['Jan', 'Feb', 'Mar'],
    datasets: [
      {
        label: role === 'ROLE_BUSINESS_OWNER' ? 'Revenue ($)' : role === 'ROLE_ADMIN' ? 'Orders Count' : 'Donations Secured',
        data: hasRevenueTrend 
          ? data.revenueTrend.map(t => t.value)
          : hasAdminTrend 
            ? data.globalOrderTrend.map(t => t.value)
            : hasNgoTrend 
              ? data.claimsTrend.map(t => t.value)
              : [0, 0, 0],
        backgroundColor: '#2e7d5b',
        borderRadius: 4,
      }
    ]
  };

  const chartOptions = {
    responsive: true,
    plugins: {
      legend: { position: 'top', labels: { color: '#fff' } },
    },
    scales: {
      x: { ticks: { color: 'rgba(255,255,255,0.7)' }, grid: { color: 'rgba(255,255,255,0.05)' } },
      y: { ticks: { color: 'rgba(255,255,255,0.7)' }, grid: { color: 'rgba(255,255,255,0.05)' } }
    }
  };

  return (
    <Box>
      <Grid container spacing={3} sx={{ mb: 4 }}>
        {role === 'ROLE_BUSINESS_OWNER' && (
          <>
            <Grid item xs={12} sm={6} md={3}>
              <MetricCard title="Total Revenue" value={`$${data.totalRevenue?.toFixed(2)}`} icon={<RevenueIcon color="primary" />} />
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <MetricCard title="Approved Donations" value={data.totalDonations} icon={<DonationIcon color="secondary" />} />
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <MetricCard title="Food Waste Saved" value={`${data.wasteSavedItems} items`} icon={<WasteIcon sx={{ color: '#00bcd4' }} />} />
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <MetricCard title="Avg Rating" value={`${data.averageRating?.toFixed(1)} / 5.0`} icon={<StarIcon sx={{ color: '#ffb300' }} />} />
            </Grid>
          </>
        )}

        {role === 'ROLE_ADMIN' && (
          <>
            <Grid item xs={12} sm={6} md={3}>
              <MetricCard title="Global Users" value={data.totalUsers} icon={<RevenueIcon color="primary" />} />
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <MetricCard title="Total Listings" value={data.totalListings} icon={<DonationIcon color="secondary" />} />
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <MetricCard title="Global Revenue" value={`$${data.globalRevenue?.toFixed(2)}`} icon={<WasteIcon sx={{ color: '#00bcd4' }} />} />
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <MetricCard title="Total Food Saved" value={`${data.globalWasteSavedItems} items`} icon={<StarIcon sx={{ color: '#ffb300' }} />} />
            </Grid>
          </>
        )}

        {role === 'ROLE_NGO' && (
          <>
            <Grid item xs={12} sm={6} md={3}>
              <MetricCard title="Total Claims" value={data.totalClaims} icon={<DonationIcon color="secondary" />} />
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <MetricCard title="Active Claims" value={data.activeClaims} icon={<RevenueIcon color="primary" />} />
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <MetricCard title="Completed Pickups" value={data.completedClaims} icon={<WasteIcon sx={{ color: '#00bcd4' }} />} />
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <MetricCard title="Food Items Secured" value={`${data.foodItemsSecured} items`} icon={<StarIcon sx={{ color: '#ffb300' }} />} />
            </Grid>
          </>
        )}
      </Grid>

      {/* Chart */}
      <Card sx={{ backgroundColor: '#181d1c', border: '1px solid rgba(255,255,255,0.06)' }}>
        <CardContent>
          <Typography variant="h6" sx={{ fontWeight: 'bold', mb: 2 }}>Monthly Performance Trend</Typography>
          <Box sx={{ height: 260, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Bar data={chartData} options={chartOptions} />
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
}

function MetricCard({ title, value, icon }) {
  return (
    <Card sx={{ backgroundColor: '#181d1c', border: '1px solid rgba(255,255,255,0.06)' }}>
      <CardContent sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Box>
          <Typography variant="caption" color="text.secondary" sx={{ textTransform: 'uppercase', letterSpacing: 0.5 }}>{title}</Typography>
          <Typography variant="h5" sx={{ fontWeight: 'bold', mt: 0.5 }}>{value}</Typography>
        </Box>
        <Box sx={{ p: 1.5, borderRadius: '50%', backgroundColor: 'rgba(255,255,255,0.04)', display: 'flex', alignItems: 'center' }}>
          {icon}
        </Box>
      </CardContent>
    </Card>
  );
}
