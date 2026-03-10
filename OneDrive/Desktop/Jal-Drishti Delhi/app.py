import streamlit as st
import folium
from streamlit_folium import st_folium
import plotly.graph_objects as go
import pandas as pd
import numpy as np
from datetime import datetime, timedelta
import base64
from io import BytesIO
from PIL import Image, ImageDraw, ImageFont

# Page Configuration
st.set_page_config(
    page_title="MCD Flood Command Center",
    page_icon="🌊",
    layout="wide",
    initial_sidebar_state="expanded"
)

# Custom CSS for Dark Mode Command Center Theme
st.markdown("""
    <style>
    /* Main Theme */
    .stApp {
        background: linear-gradient(135deg, #0f0c29, #302b63, #24243e);
    }
    
    /* Headers */
    h1 {
        color: #00d4ff !important;
        text-align: center;
        text-shadow: 0 0 20px #00d4ff;
        font-family: 'Courier New', monospace;
        padding: 20px;
        background: rgba(0, 212, 255, 0.1);
        border-radius: 10px;
        border: 2px solid #00d4ff;
    }
    
    h2, h3 {
        color: #00ff88 !important;
        text-shadow: 0 0 10px #00ff88;
    }
    
    /* Metric Cards */
    [data-testid="stMetricValue"] {
        font-size: 2.5rem;
        color: #00d4ff;
    }
    
    [data-testid="stMetricLabel"] {
        color: #ffffff !important;
        font-weight: bold;
    }
    
    /* Sidebar */
    [data-testid="stSidebar"] {
        background: linear-gradient(180deg, #1a1a2e, #16213e);
        border-right: 2px solid #00d4ff;
    }
    
    /* Buttons */
    .stButton>button {
        width: 100%;
        background: linear-gradient(90deg, #ff006e, #ff6b00);
        color: white;
        border: none;
        padding: 15px;
        font-size: 16px;
        font-weight: bold;
        border-radius: 8px;
        cursor: pointer;
        transition: all 0.3s;
        box-shadow: 0 4px 15px rgba(255, 0, 110, 0.4);
    }
    
    .stButton>button:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(255, 0, 110, 0.6);
    }
    
    /* Info boxes */
    .stAlert {
        background: rgba(0, 212, 255, 0.1);
        border-left: 4px solid #00d4ff;
        color: white;
    }
    
    /* Cards */
    div[data-testid="stVerticalBlock"] > div {
        background: rgba(255, 255, 255, 0.05);
        padding: 15px;
        border-radius: 10px;
        border: 1px solid rgba(0, 212, 255, 0.3);
    }
    </style>
""", unsafe_allow_html=True)

# Initialize session state
if 'rainfall' not in st.session_state:
    st.session_state.rainfall = 0
if 'alert_sent' not in st.session_state:
    st.session_state.alert_sent = False
if 'pumps_deployed' not in st.session_state:
    st.session_state.pumps_deployed = 0

# High-Risk Hotspots Data
HOTSPOTS = [
    {"name": "Minto Road", "lat": 28.6289, "lon": 77.2334, "elevation": 212, "base_risk": 0.85},
    {"name": "Kashmere Gate", "lat": 28.6692, "lon": 77.2289, "elevation": 215, "base_risk": 0.78},
    {"name": "Kirari", "lat": 28.7747, "lon": 77.0371, "elevation": 208, "base_risk": 0.92},
    {"name": "ITO", "lat": 28.6289, "lon": 77.2497, "elevation": 211, "base_risk": 0.88},
    {"name": "Najafgarh", "lat": 28.6092, "lon": 76.9798, "elevation": 205, "base_risk": 0.95},
    {"name": "Yamuna Bank", "lat": 28.6414, "lon": 77.2833, "elevation": 209, "base_risk": 0.82},
    {"name": "Safdarjung Enclave", "lat": 28.5562, "lon": 77.1901, "elevation": 216, "base_risk": 0.71},
    {"name": "Pul Prahladpur", "lat": 28.4877, "lon": 77.2892, "elevation": 207, "base_risk": 0.89},
    {"name": "Gandhi Nagar", "lat": 28.6631, "lon": 77.2464, "elevation": 213, "base_risk": 0.76},
    {"name": "Tilak Nagar", "lat": 28.6414, "lon": 77.0955, "elevation": 210, "base_risk": 0.84}
]

# Drainage Mock Data
DRAINAGE_SYSTEMS = [
    {
        "name": "Najafgarh Drain",
        "status": "Silted",
        "confidence": 87.3,
        "last_cleaned": "2 months ago",
        "capacity": "68%"
    },
    {
        "name": "Barapullah Drain",
        "status": "Clear",
        "confidence": 94.1,
        "last_cleaned": "1 week ago",
        "capacity": "92%"
    },
    {
        "name": "Gazipur Drain",
        "status": "Blocked",
        "confidence": 91.8,
        "last_cleaned": "4 months ago",
        "capacity": "34%"
    }
]

def calculate_flood_risk(base_risk, rainfall):
    """Calculate dynamic flood risk based on rainfall"""
    rainfall_factor = min(rainfall / 100, 1.5)
    risk = min(base_risk * (1 + rainfall_factor), 1.0)
    return risk

def get_risk_color(risk):
    """Return color based on risk level"""
    if risk < 0.3:
        return 'green'
    elif risk < 0.6:
        return 'orange'
    else:
        return 'red'

def create_mock_cctv_image(drain_name, status):
    """Create a mock CCTV feed image"""
    img = Image.new('RGB', (400, 300), color=(20, 20, 40))
    draw = ImageDraw.Draw(img)
    
    # Add some random elements to simulate drain
    if status == "Blocked":
        # Draw blocked drain
        for _ in range(30):
            x = np.random.randint(50, 350)
            y = np.random.randint(50, 250)
            draw.ellipse([x, y, x+20, y+20], fill=(139, 69, 19))
    elif status == "Silted":
        # Draw partially silted
        for _ in range(15):
            x = np.random.randint(50, 350)
            y = np.random.randint(150, 250)
            draw.ellipse([x, y, x+15, y+15], fill=(160, 82, 45))
    else:
        # Draw clear drain
        draw.rectangle([100, 100, 300, 200], fill=(30, 60, 90))
    
    # Add timestamp
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    draw.text((10, 10), f"CCTV: {drain_name}", fill=(0, 255, 136))
    draw.text((10, 270), timestamp, fill=(0, 212, 255))
    
    return img

def generate_prediction_data():
    """Generate LSTM-like prediction data"""
    current_time = datetime.now()
    times = [current_time + timedelta(hours=i) for i in range(7)]
    
    # Current water level
    current_level = 2.1
    
    # Prediction based on rainfall
    rainfall_impact = st.session_state.rainfall / 50
    
    # Historical data (past hour)
    historical_levels = [current_level - 0.3, current_level - 0.15, current_level]
    
    # Predicted levels
    predicted_levels = []
    level = current_level
    for i in range(1, 4):
        level += (rainfall_impact * 0.3 + np.random.uniform(-0.1, 0.2))
        predicted_levels.append(level)
    
    all_times = [current_time - timedelta(hours=2), current_time - timedelta(hours=1), current_time] + times[1:4]
    all_levels = historical_levels + predicted_levels
    
    return all_times, all_levels, current_level, predicted_levels[-1]

# ============= HEADER =============
st.markdown("""
    <h1>🌊 MCD: Urban Flooding & Hydrology Command Center</h1>
    <p style='text-align: center; color: #00ff88; font-size: 1.2rem; margin-top: -15px;'>
        India Innovates 2026 | Real-Time Flood Intelligence System
    </p>
""", unsafe_allow_html=True)

# ============= SIDEBAR =============
with st.sidebar:
    st.markdown("### ⚙️ SIMULATION CONTROLS")
    st.markdown("---")
    
    # Rainfall Slider
    rainfall = st.slider(
        "🌧️ Rainfall Intensity (mm)",
        min_value=0,
        max_value=150,
        value=st.session_state.rainfall,
        step=5,
        help="Adjust rainfall to see dynamic flood risk changes"
    )
    st.session_state.rainfall = rainfall
    
    st.markdown("---")
    st.markdown("### 📊 SYSTEM STATUS")
    
    # Calculate active alerts
    active_alerts = sum(1 for spot in HOTSPOTS if calculate_flood_risk(spot['base_risk'], rainfall) > 0.75)
    
    st.metric("💧 Rainfall", f"{rainfall} mm")
    st.metric("⚠️ Active Alerts", active_alerts)
    st.metric("🚨 Pumps Deployed", st.session_state.pumps_deployed)
    
    st.markdown("---")
    st.markdown("### 📡 LIVE DATA")
    st.info(f"🕐 **Last Updated:** {datetime.now().strftime('%H:%M:%S')}")
    st.success("✅ **System Status:** ONLINE")
    st.warning("⚡ **AI Model:** ACTIVE")

# ============= MAIN DASHBOARD =============

# Top Metrics Row
col1, col2, col3, col4 = st.columns(4)

with col1:
    st.metric(
        "📊 Avg Rainfall (24h)",
        f"{rainfall * 0.6:.1f} mm",
        delta=f"{rainfall * 0.1:.1f} mm"
    )

with col2:
    st.metric(
        "🚨 Active Flood Alerts",
        active_alerts,
        delta=f"{active_alerts - 2} new" if active_alerts > 2 else "Normal"
    )

with col3:
    st.metric(
        "⚙️ Pumps Deployed",
        st.session_state.pumps_deployed,
        delta=f"{st.session_state.pumps_deployed} active"
    )

with col4:
    drainage_health = sum(1 for d in DRAINAGE_SYSTEMS if d['status'] == 'Clear')
    st.metric(
        "🔧 Drainage Health",
        f"{drainage_health}/3 Clear",
        delta="Good" if drainage_health >= 2 else "⚠️ Critical"
    )

st.markdown("---")

# ============= INTERACTIVE GIS MAP =============
st.markdown("## 🗺️ Real-Time Flood Risk Map - Delhi NCR")

# Create base map
m = folium.Map(
    location=[28.6139, 77.2090],
    zoom_start=11,
    tiles='CartoDB dark_matter'
)

# Add hotspots to map
for spot in HOTSPOTS:
    risk = calculate_flood_risk(spot['base_risk'], rainfall)
    color = get_risk_color(risk)
    
    # Create popup HTML
    popup_html = f"""
    <div style='width: 200px; font-family: Arial;'>
        <h4 style='color: {color}; margin: 5px 0;'>{spot['name']}</h4>
        <hr style='margin: 5px 0;'>
        <p><b>Flood Risk:</b> {risk*100:.1f}%</p>
        <p><b>Elevation:</b> {spot['elevation']}m</p>
        <p><b>Status:</b> {"🔴 CRITICAL" if risk > 0.75 else "🟡 MODERATE" if risk > 0.5 else "🟢 SAFE"}</p>
    </div>
    """
    
    # Add circle marker
    folium.CircleMarker(
        location=[spot['lat'], spot['lon']],
        radius=15 if risk > 0.75 else 10,
        color=color,
        fill=True,
        fillColor=color,
        fillOpacity=0.7,
        popup=folium.Popup(popup_html, max_width=250),
        tooltip=f"{spot['name']} - Risk: {risk*100:.0f}%"
    ).add_to(m)
    
    # Add pulsing effect for high-risk areas
    if risk > 0.75:
        folium.Circle(
            location=[spot['lat'], spot['lon']],
            radius=500,
            color=color,
            fill=True,
            fillOpacity=0.2
        ).add_to(m)

# Display map
st_folium(m, width=None, height=500)

# Map Legend
col1, col2, col3 = st.columns(3)
with col1:
    st.markdown("🟢 **Safe** (Risk < 50%)")
with col2:
    st.markdown("🟡 **Moderate** (Risk 50-75%)")
with col3:
    st.markdown("🔴 **Critical** (Risk > 75%)")

st.markdown("---")

# ============= AI DRAINAGE HEALTH MONITOR =============
st.markdown("## 🤖 AI Drainage Health Monitor")
st.markdown("*Real-time AI Image Recognition from 2,152 km of MCD drainage network*")

cols = st.columns(3)

for idx, drain in enumerate(DRAINAGE_SYSTEMS):
    with cols[idx]:
        st.markdown(f"### 📹 {drain['name']}")
        
        # Generate and display mock CCTV feed
        img = create_mock_cctv_image(drain['name'], drain['status'])
        st.image(img, use_container_width=True)
        
        # Status badge
        status_color = {
            "Clear": "🟢",
            "Silted": "🟡",
            "Blocked": "🔴"
        }
        
        st.markdown(f"""
        <div style='background: rgba(0,0,0,0.3); padding: 15px; border-radius: 8px; border-left: 4px solid {"#00ff00" if drain["status"] == "Clear" else "#ff0000" if drain["status"] == "Blocked" else "#ffaa00"}'>
            <p style='margin: 5px 0;'><b>Status:</b> {status_color[drain['status']]} {drain['status']}</p>
            <p style='margin: 5px 0;'><b>AI Confidence:</b> {drain['confidence']}%</p>
            <p style='margin: 5px 0;'><b>Capacity:</b> {drain['capacity']}</p>
            <p style='margin: 5px 0;'><b>Last Cleaned:</b> {drain['last_cleaned']}</p>
        </div>
        """, unsafe_allow_html=True)
        
        # Recommended action
        if drain['status'] == "Blocked":
            st.error("⚠️ **Immediate desilting required**")
        elif drain['status'] == "Silted":
            st.warning("🔧 **Schedule maintenance soon**")
        else:
            st.success("✅ **Operating normally**")

st.markdown("---")

# ============= PREDICTIVE LSTM GRAPH =============
st.markdown("## 📈 Predictive Water Level Forecast (LSTM Model)")
st.markdown("*AI-powered 6-hour water level prediction*")

# Generate prediction data
times, levels, current_level, predicted_level = generate_prediction_data()

# Create plotly figure
fig = go.Figure()

# Historical data (solid line)
fig.add_trace(go.Scatter(
    x=times[:3],
    y=levels[:3],
    mode='lines+markers',
    name='Historical Level',
    line=dict(color='#00d4ff', width=3),
    marker=dict(size=10)
))

# Predicted data (dashed line)
fig.add_trace(go.Scatter(
    x=times[2:],
    y=levels[2:],
    mode='lines+markers',
    name='Predicted Level',
    line=dict(color='#ff006e', width=3, dash='dash'),
    marker=dict(size=10, symbol='diamond')
))

# Danger threshold
fig.add_hline(
    y=3.5,
    line_dash="dot",
    line_color="red",
    annotation_text="⚠️ Danger Level",
    annotation_position="right"
)

# Update layout
fig.update_layout(
    title="Water Level vs Time",
    xaxis_title="Time",
    yaxis_title="Water Level (meters)",
    template="plotly_dark",
    hovermode='x unified',
    height=400,
    showlegend=True,
    paper_bgcolor='rgba(0,0,0,0)',
    plot_bgcolor='rgba(0,0,0,0.2)',
    font=dict(color='#ffffff')
)

st.plotly_chart(fig, use_container_width=True)

# Prediction Summary
col1, col2, col3 = st.columns(3)
with col1:
    st.metric("Current Water Level", f"{current_level:.2f} m")
with col2:
    st.metric("Predicted Level (3hrs)", f"{predicted_level:.2f} m", delta=f"{predicted_level - current_level:+.2f} m")
with col3:
    if predicted_level > 3.5:
        st.error("⚠️ **FLOOD RISK in 2-3 hours**")
    elif predicted_level > 3.0:
        st.warning("⚡ **Monitor Closely**")
    else:
        st.success("✅ **Normal Conditions**")

st.markdown("---")

# ============= ADMIN ACTION PANEL =============
st.markdown("## 🎯 One-Click Emergency Response Panel")

col1, col2, col3 = st.columns(3)

with col1:
    if st.button("🚨 Deploy Emergency Pumps"):
        st.session_state.pumps_deployed += 5
        st.success(f"✅ Successfully deployed 5 pumps! Total: {st.session_state.pumps_deployed}")
        st.balloons()

with col2:
    if st.button("📢 Send Traffic Alert to Delhi Police"):
        st.session_state.alert_sent = True
        st.success("✅ Alert sent to Delhi Traffic Police HQ!")
        st.info("""
        **Alert Message:**
        - High flood risk detected
        - Routes affected: Minto Road, ITO, Kashmere Gate
        - Requested traffic diversion
        """)

with col3:
    if st.button("📄 Generate Ward-wise PDF Report"):
        st.success("✅ Generating comprehensive report...")
        
        # Create mock report data
        report_data = {
            "Ward": ["Zone 1", "Zone 2", "Zone 3", "Zone 4", "Zone 5"],
            "Risk Level": ["High", "Medium", "Low", "High", "Medium"],
            "Hotspots": [3, 2, 1, 4, 2],
            "Action Required": ["Yes", "No", "No", "Yes", "No"]
        }
        df = pd.DataFrame(report_data)
        st.dataframe(df, use_container_width=True)
        
        st.download_button(
            label="📥 Download PDF Report",
            data=df.to_csv(index=False),
            file_name=f"MCD_Flood_Report_{datetime.now().strftime('%Y%m%d_%H%M%S')}.csv",
            mime="text/csv"
        )

st.markdown("---")

# ============= FOOTER =============
st.markdown("""
<div style='text-align: center; padding: 20px; background: rgba(0,0,0,0.3); border-radius: 10px; margin-top: 30px;'>
    <h3 style='color: #00d4ff;'>🏆 India Innovates 2026 - Jal-Drishti Delhi</h3>
    <p style='color: #00ff88;'>Municipal Corporation of Delhi - Urban Flooding & Hydrology Intelligence</p>
    <p style='color: #ffffff; opacity: 0.7;'>Powered by AI • Real-time Analytics • Predictive Intelligence</p>
</div>
""", unsafe_allow_html=True)

# Auto-refresh option
if st.sidebar.checkbox("🔄 Auto-refresh (Every 5 sec)", value=False):
    st.rerun()
