# 🌊 Jal-Drishti Delhi - Urban Flooding & Hydrology Command Center

**India Innovates 2026 Hackathon Project**

An AI-powered, real-time flood prediction and management system for the Municipal Corporation of Delhi (MCD).

![Streamlit](https://img.shields.io/badge/Streamlit-FF4B4B?style=for-the-badge&logo=streamlit&logoColor=white)
![Python](https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white)
![AI](https://img.shields.io/badge/AI-Powered-00D4FF?style=for-the-badge)

---

## ⚡ **QUICK START** (For Impatient People!)

```bash
# Just run this - everything is already installed!
streamlit run app_full.py
```

**OR** double-click: `RUN.bat`

**Browser will open at:** http://localhost:8501

**YOU'RE DONE!** ✅ The app has 6 pages - use sidebar buttons to navigate!

---

## 🎯 Project Overview

**Jal-Drishti Delhi** transforms urban flood management from reactive to predictive. Using real-time rainfall data, AI-powered drainage monitoring, and LSTM-based water level predictions, this system helps MCD prevent flooding before it happens.

### Key Features

✅ **Multi-Page Navigation System**
- 6 interactive pages with seamless navigation
- Home Dashboard, Hotspots, Drainage, Analytics, Historical Data, Emergency Actions
- Professional command center interface

✅ **10 Delhi Flood Hotspots - Detailed Monitoring**
- Real-time risk calculation for each zone
- Individual trend graphs for past 6 hours
- Population data, elevation, and coordinates
- Expandable cards with full details
- Per-zone action buttons (deploy pumps, traffic alerts, resident notifications)

✅ **AI Drainage Health Monitor**
- 6 major drains with live monitoring (2,152 km network)
- Simulated CCTV feed analysis
- AI classification: Clear, Silted, or Blocked
- Confidence scores and maintenance recommendations
- 24-hour flow rate charts

✅ **Advanced LSTM Predictions & Analytics**
- 6-hour water level forecasting with confidence intervals
- Risk distribution across zones
- Rainfall vs flood incidents correlation analysis
- Model accuracy and confidence metrics

✅ **10 Years Historical Data Analysis**
- Comprehensive data from 2016-2026
- Yearly and seasonal trend analysis
- Most affected areas database
- Downloadable CSV datasets

✅ **Comprehensive Emergency Response Panel**
- One-click pump deployment
- Mass SMS alert system
- Traffic diversion protocols
- NDRF deployment requests
- Evacuation management
- Situation report generation

✅ **Dark Mode Command Center UI**
- Professional government dashboard theme
- Real-time metrics and status indicators
- Responsive design
- Clean interface (Deploy button hidden)

---

## 🚀 Quick Start

### Prerequisites

- Python 3.8 or higher
- pip package manager

### Installation

1. **Clone or Download the Project**
```bash
cd "c:\Users\HP\OneDrive\Desktop\Jal-Drishti Delhi"
```

2. **Create Virtual Environment (Recommended)**
```bash
python -m venv venv
```

3. **Activate Virtual Environment**

Windows:
```bash
venv\Scripts\activate
```

Mac/Linux:
```bash
source venv/bin/activate
```

4. **Install Dependencies**
```bash
pip install streamlit plotly pandas numpy Pillow
```

**Note:** All required packages (streamlit, plotly, pandas, numpy, Pillow) are already installed on your system!

5. **Run the Application**

**Option A - Double-click:** `RUN.bat`

**Option B - Terminal:**
```bash
streamlit run app_full.py
```

The application will automatically open in your default browser at `http://localhost:8501`

---

## 📦 Dependencies

All packages are **already installed** on your system!

| Package | Purpose |
|---------|---------|
| streamlit | Web framework & UI |
| plotly | Interactive graphs & charts |
| pandas | Data manipulation & tables |
| numpy | Numerical computing |
| Pillow | Image processing (CCTV feeds) |

**No additional installations required!** ✅

---

## 🎮 How to Use

### Navigation
- Use **sidebar buttons** to switch between 6 pages
- Adjust **rainfall slider** (affects all pages dynamically)
- **Expandable cards** - click arrows to see detailed data

### 1. 🏠 Home Dashboard
- View top-level metrics (rainfall, alerts, pumps, drainage health)
- Quick access cards to jump to other pages
- Recent alerts table
- Live water level prediction preview

### 2. 🗺️ Flood Hotspots Details
- **10 high-risk zones** across Delhi
- Click to **expand each zone** for full details
- View individual **6-hour risk trend graphs**
- Population, elevation, coordinates for each hotspot
- **Action buttons**: Deploy pumps, traffic diversion, resident alerts

### 3. 🚰 Drainage Network
- Monitor **6 major drains** (2,152 km total network)
- Simulated **live CCTV feeds**
- **AI confidence scores**: Clear / Silted / Blocked status
- **24-hour flow rate charts** for each drain
- Schedule maintenance, view reports, set alerts

### 4. 📈 Analytics & Predictions
- **LSTM 6-hour water level forecast** (historical + predicted)
- Danger level thresholds (3.0m warning, 3.5m danger)
- **Risk distribution bar chart** across Delhi zones
- **Rainfall vs incidents correlation** (monthly analysis)
- Model accuracy and confidence metrics

### 5. 📊 Historical Data
- **10 years of data** (2016-2026)
- **Yearly trend chart** of flood incidents
- **Seasonal distribution** (monthly averages)
- Most affected areas table
- **Download complete CSV dataset**

### 6. 🎯 Emergency Actions
- **One-click responses**: Deploy pumps, traffic alerts, mass SMS
- **NDRF deployment** request
- **Evacuation management** for high-risk zones
- **Situation report** generation & download
- Equipment status tracking
- Emergency contact directory

---

## 🌐 Deployment Options

### Option 1: Streamlit Community Cloud (FREE)

1. Create account at [streamlit.io](https://streamlit.io)
2. Connect your GitHub repository
3. Deploy with one click
4. Get a public URL instantly

### Option 2: Heroku

```bash
# Create Procfile
echo "web: streamlit run app_full.py --server.port=$PORT --server.address=0.0.0.0" > Procfile

# Deploy
heroku create jal-drishti-delhi
git push heroku main
```

### Option 3: AWS EC2

```bash
# Install dependencies
sudo apt update
sudo apt install python3-pip
pip3 install streamlit plotly pandas numpy Pillow

# Run in background
nohup streamlit run app_full.py --server.port=80 &
```

### Option 4: Docker

```dockerfile
FROM python:3.9-slim
WORKDIR /app
COPY . .
RUN pip install streamlit plotly pandas numpy Pillow
EXPOSE 8501
CMD ["streamlit", "run", "app_full.py"]
```
git push heroku main
```

### Option 3: AWS EC2

```bash
# Install dependencies
sudo apt update
sudo apt install python3-pip
pip3 install -r requirements.txt

# Run in background
nohup streamlit run app.py --server.port=80 &
```

### Option 4: Docker

```dockerfile
FROM python:3.9-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install -r requirements.txt
COPY . .
EXPOSE 8501
CMD ["streamlit", "run", "app.py"]
```

---

## 🏗️ Project Structure

```
Jal-Drishti Delhi/
│
├── app_full.py           # ⭐ Main multi-page application (USE THIS!)
├── app_simple.py         # Simple single-page version
├── app_nomap.py          # Version without folium (backup)
├── app.py                # Original version (requires folium - not working)
├── requirements.txt      # Python dependencies list
├── RUN.bat              # Quick launcher for Windows
├── start_lite.bat       # Launcher for lite version
├── README.md            # Complete documentation (this file)
├── QUICKSTART.md        # 2-minute quick start guide
├── LITE_VERSION.md      # Guide for no-map version
├── TROUBLESHOOTING.md   # Common issues & solutions
└── .streamlit/          # Streamlit config (auto-generated)
```

**Main File to Run:** `app_full.py` (6 pages, no external dependencies needed!)

---

## 🎨 Features Breakdown

### Multi-Page Architecture
- **Technology**: Streamlit session state + dynamic rerun
- **Navigation**: Sidebar buttons with page switching
- **6 Full Pages**: Home, Hotspots, Drainage, Analytics, Historical, Emergency
- **Persistent State**: Rainfall and pump settings carry across pages

### 10 Flood Hotspots Monitoring
- **Real Delhi Zones**: Minto Road, Kashmere Gate, Kirari, ITO, Najafgarh, etc.
- **Dynamic Risk**: Calculated as `base_risk × (1 + rainfall_factor)`
- **Per-Zone Details**: Population, elevation, coordinates, area
- **Trend Graphs**: 6-hour risk history for each zone
- **Color Coding**: 
  - 🟢 Green: Risk < 30%
  - 🟡 Orange: Risk 30-60%
  - 🔴 Red: Risk > 60%

### AI Drainage Monitor
- **Mock CCTV**: Programmatically generated drain images
- **AI Simulation**: Status classification with confidence scores
- **Real Drains**: Najafgarh, Barapullah, Gazipur
- **Metrics**: Capacity %, Last cleaned date

### LSTM Prediction
- **Input**: Current rainfall intensity
- **Output**: 3-hour water level forecast
- **Visualization**: Plotly interactive graph
- **Alert System**: Automated warnings when predicted level > 3.5m

### Emergency Response
- **Pump Deployment**: Increment pump count
- **Traffic Alerts**: Simulated API call to traffic police
- **Report Generation**: CSV download with ward-wise data

---

## 🔧 Customization

### Add More Hotspots

Edit the `hotspots` list in `app_full.py` (around line 180):

```python
hotspots = [
    {"name": "Your Area", "lat": 28.XXX, "lon": 77.XXX, "elevation": 210, 
     "risk": 0.80, "population": "50,000", "area": "2.3 km²"},
    # Add more zones...
]
```

### Add More Drains to Monitor

Edit the `drains` list in `app_full.py` (around line 285):

```python
drains = [
    {"name": "Your Drain", "length": "15 km", "status": "Clear", 
     "confidence": 88.5, "capacity": "85%", "flow": "150 m³/s", 
     "last_cleaned": "1 week ago"},
    # Add more drains...
]
```

### Customize Rainfall Risk Formula

Update the `calculate_flood_risk()` function (near top of file):

```python
rainfall_impact = st.session_state.rainfall / 50  # Change sensitivity
level += (rainfall_impact * 0.3)  # Adjust growth rate
```

### Change Color Theme

Modify the CSS in `app_full.py` (lines 20-50):

```python
st.markdown("""
<style>
.stApp { background: YOUR_GRADIENT_HERE; }
h1 { color: YOUR_COLOR !important; }
# Customize colors, fonts, spacing...
</style>
""", unsafe_allow_html=True)
```

---

## 📊 Mock Data Sources

This prototype uses simulated data for demonstration:

- **Hotspots**: Based on historical MCD flood reports
- **CCTV Images**: Programmatically generated using Pillow
- **Water Levels**: Mathematical simulation of LSTM predictions
- **Drainage Status**: Random assignment with realistic parameters

### For Production:
- Integrate IMD (India Meteorological Department) API
- Connect real CCTV feeds with TensorFlow/PyTorch model
- Use actual DEM (Digital Elevation Model) data
- Implement real LSTM trained on historical flood data

---

## 🏆 Hackathon Presentation Tips (5-7 Minute Demo)

**Opening (30 sec)**
1. **Start with Impact**: "Delhi flooding affects 500,000+ people annually, causes ₹450 Cr damage"
2. **The Solution**: "Jal-Drishti shifts from reactive to predictive flood management"

**Demo Flow (4-5 min)**
3. **Home Dashboard (45 sec)**: Show metrics, click "View All Flood Hotspots"
4. **Hotspots Page (1 min)**: Expand Minto Road → show trend graph → deploy pumps
5. **Adjust Rainfall (30 sec)**: Move slider from 20mm → 100mm → watch all zones turn red
6. **Drainage Network (1 min)**: Expand Najafgarh Drain → show AI status, flow chart
7. **Analytics (1 min)**: Show LSTM 6-hour prediction, risk distribution chart
8. **Historical Data (30 sec)**: Show 10-year trends, download CSV
9. **Emergency Actions (45 sec)**: Deploy pumps, send alerts, generate report

**Closing (30 sec)**
10. **Highlight Scale**: "Monitors 2,152 km network, 10 zones, 500K+ population"
11. **Emphasize Tech**: "Multi-page UI, LSTM predictions, AI monitoring, real-time analytics"
12. **Impact**: "Prevents flooding BEFORE it happens, saves lives and property"

**Pro Tips**:
- Keep rainfall at 80-100mm during demo (shows critical zones)
- Have all pages loaded before presenting
- Emphasize the "6 full pages" of functionality

---

## 🔮 Future Enhancements

- [ ] Real-time IMD rainfall data integration
- [ ] TensorFlow-based actual LSTM model
- [ ] WhatsApp/SMS alert system for citizens
- [ ] Mobile app (React Native)
- [ ] Integration with Delhi Traffic Police API
- [ ] Historical flood data analytics dashboard
- [ ] Crowdsourced waterlogging reports
- [ ] 3D terrain visualization
- [ ] Multi-language support (Hindi, Punjabi)

---

## 🤝 Contributing

This is a hackathon project, but contributions are welcome!

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📜 License

This project is created for the **India Innovates 2026** hackathon.

---

## 👥 Team

Created for the Municipal Corporation of Delhi (MCD) Urban Flooding Challenge

---

## 📞 Support

For questions or issues:
- Open an issue on GitHub
- Email: [your-email@example.com]

---

## 🙏 Acknowledgments

- Municipal Corporation of Delhi for the problem statement
- India Innovates 2026 organizing committee
- OpenStreetMap contributors
- Streamlit community

---

<div align="center">

### 🌊 Jal-Drishti: From Observation to Prediction

**Built with ❤️ using Python & Streamlit**

[Demo](http://localhost:8501) | [Report Issue](#) | [Request Feature](#)

</div>
