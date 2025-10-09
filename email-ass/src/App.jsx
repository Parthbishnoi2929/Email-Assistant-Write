import { useState } from 'react'
import './App.css'
import {
  Box,
  Container,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  TextField,
  Typography,
  Button,
  CircularProgress,
  Paper,
  IconButton,
  Tooltip,
  Alert
} from '@mui/material'
import ContentCopyIcon from '@mui/icons-material/ContentCopy'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import axios from 'axios'

function App() {
  const [email, setemail] = useState('')
  const [tone, settone] = useState('')
  const [genratedReply, setgeneratedReply] = useState('')
  const [loading, setloading] = useState(false)
  const [copied, setCopied] = useState(false)
  const [error, setError] = useState('')   // ✅ error state

  const handlesubmit = async () => {
    if (!email.trim()) {
      alert('Please enter the original email content!')
      return
    }

    setloading(true)
    setgeneratedReply('')
    setCopied(false)
    setError('') // clear previous error

    try {
      // ✅ API call using axios
      const res = await axios.post('http://localhost:8080/api/email/generate', {
        email,
        tone
      })

      setgeneratedReply(typeof res.data==='string' ?res.data : JSON.stringify(res.data)) // assume backend sends { reply: "..." }
    } catch (err) {
      console.error(err)
      setError('❌ Failed to generate reply. Please try again.')
    } finally {
      setloading(false)
    }
  }

  const handleCopy = () => {
    navigator.clipboard.writeText(genratedReply)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  return (
    <Container maxWidth="md" sx={{ py: 5 }}>
      <Paper elevation={4} sx={{ p: 4, borderRadius: 3 }}>
        <Typography
          variant="h3"
          component="h1"
          gutterBottom
          sx={{ textAlign: 'center', fontWeight: 'bold', color: 'primary.main' }}
        >
          ✨ Email Reply Generator
        </Typography>

        <Box sx={{ mx: 2 }}>
          <TextField
            fullWidth
            multiline
            rows={6}
            variant="outlined"
            label="Original Mail Content"
            value={email}
            onChange={(e) => setemail(e.target.value)}
            sx={{ mb: 3 }}
          />

          <FormControl fullWidth sx={{ mb: 3 }}>
            <InputLabel>Tone (optional)</InputLabel>
            <Select
              value={tone}
              label="Tone"
              onChange={(e) => settone(e.target.value)}
            >
              <MenuItem value="">None</MenuItem>
              <MenuItem value="Professional">Professional</MenuItem>
              <MenuItem value="Casual">Casual</MenuItem>
              <MenuItem value="Friendly">Friendly</MenuItem>
              <MenuItem value="Polite">Polite</MenuItem>
              <MenuItem value="Direct">Direct</MenuItem>
            </Select>
          </FormControl>

          <Box sx={{ textAlign: 'center' }}>
            <Button
              variant="contained"
              color="primary"
              size="large"
              sx={{ px: 4, py: 1.5, borderRadius: 3, fontWeight: 'bold' }}
              onClick={handlesubmit}
              disabled={loading}
            >
              {loading ? (
                <CircularProgress size={28} color="inherit" />
              ) : (
                '🚀 Generate Reply'
              )}
            </Button>
          </Box>

          {/* ✅ Show error message */}
          {error && (
            <Alert severity="error" sx={{ mt: 2 }}>
              {error}
            </Alert>
          )}
        </Box>
      </Paper>

      {genratedReply && (
        <Paper
          elevation={3}
          sx={{
            mt: 4,
            p: 3,
            borderRadius: 2,
            backgroundColor: '#f9f9f9',
            position: 'relative'
          }}
        >
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Typography variant="h6" sx={{ color: 'secondary.main' }}>
              📝 Generated Reply:
            </Typography>
            <Tooltip title={copied ? 'Copied!' : 'Copy to clipboard'}>
              <IconButton onClick={handleCopy} color={copied ? 'success' : 'default'}>
                {copied ? <CheckCircleIcon /> : <ContentCopyIcon />}
              </IconButton>
            </Tooltip>
          </Box>
          <Typography sx={{ whiteSpace: 'pre-line', fontSize: '1.05rem', mt: 1 }}>
            {genratedReply}
          </Typography>
        </Paper>
      )}
    </Container>
  )
}

export default App
