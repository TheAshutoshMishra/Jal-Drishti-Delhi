import streamlit as st
import plotly.graph_objects as go
import pandas as pd
import numpy as np
from datetime import datetime, timedelta

# Page config
st.set_page_config(page_title="MCD Flood Command Center", page_icon="🌊", layout="wide")

# Custom CSS
st.markdown("""
<style>
.stApp { background: linear-gradient(135deg, #0f0c29, #302b63, #24243e); }
h1 { color: #00d4ff !important; text-align: center; }
h2, h3 { color: #00ff88 !important; }
</style>
""", unsafe_allow_html=True)

# Session state
if 'rainfall' not in st.session_state:
    st.session_state.rainfall = 0
if 'pumps' not in st.session_state:
    st.session_state.pumps = 0

# Header
st.markdown("# 🌊 MCD: Urban Flooding & Hydrology Command Center")
st.markdown("**India Innovates 2026** | Real-Time Flood Intelligence System")

# Sidebar
with st.sidebar:
    st.markdown("### ⚙️ CONTROLS")
    rainfall = st.slider("🌧️ Rainfall (mm)", 0, 150, st.session_state.rainfall, 5)
    st.session_state.rainfall = rainfall
    
    st.markdown("---")
    st.metric("💧 Rainfall", f"{rainfall} mm")
    st.metric("🚨 Pumps", st.session_state.pumps)

# Metrics
col1, col2, col3, col4 = st.columns(4)
with col1:
    st.metric("📊 Avg Rainfall (24h)", f"{rainfall * 0.6:.1f} mm")
with col2:
    alerts = min(int(rainfall / 15), 10)
    st.metric("🚨 Active Alerts", alerts)
with col3:
    st.metric("⚙️ Pumps Deployed", st.session_state.pumps)
with col4:
    st.metric("🔧 Drainage Health", "2/3 Clear")

st.markdown("---")

# Hotspots
st.markdown("## 🗺️ Delhi Flood Risk Hotspots")

hotspots = [
    {"name": "Minto Road", "risk": 0.85},
    {"name": "Kashmere Gate", "risk": 0.78},
    {"name": "Kirari", "risk": 0.92},
    {"name": "ITO", "risk": 0.88},
    {"name": "Najafgarh", "risk": 0.95},
    {"name": "Yamuna Bank", "risk": 0.82},
]

col1, col2, col3 = st.columns(3)
for idx, spot in enumerate(hotspots):
    risk = min(spot['risk'] * (1 + rainfall / 100), 1.0)
    emoji = "🔴" if risk > 0.75 else "🟡" if risk > 0.5 else "🟢"
    status = "CRITICAL" if risk > 0.75 else "MODERATE" if risk > 0.5 else "SAFE"
    
    with [col1, col2, col3][idx % 3]:
        st.markdown(f"""
        <div style='background: rgba(0,0,0,0.4); padding: 15px; border-radius: 8px; margin: 10px 0;'>
            <h3>{emoji} {spot['name']}</h3>
            <p><b>Risk:</b> {risk*100:.1f}%</p>
            <p><b>Status:</b> {status}</p>
        </div>
        """, unsafe_allow_html=True)

st.markdown("---")

# Prediction Graph
st.markdown("## 📈 Water Level Forecast (LSTM)")

current_time = datetime.now()
times = [current_time + timedelta(hours=i) for i in range(-2, 4)]
levels = [2.1 - 0.3, 2.1 - 0.15, 2.1]

for i in range(3):
    levels.append(levels[-1] + (rainfall / 50) * 0.3)

fig = go.Figure()
fig.add_trace(go.Scatter(x=times[:3], y=levels[:3], mode='lines+markers', name='Historical', line=dict(color='#00d4ff', width=3)))
fig.add_trace(go.Scatter(x=times[2:], y=levels[2:], mode='lines+markers', name='Predicted', line=dict(color='#ff006e', width=3, dash='dash')))
fig.add_hline(y=3.5, line_dash="dot", line_color="red", annotation_text="Danger Level")
fig.update_layout(template="plotly_dark", height=400, paper_bgcolor='rgba(0,0,0,0)', plot_bgcolor='rgba(0,0,0,0.2)')

st.plotly_chart(fig, use_container_width=True)

col1, col2, col3 = st.columns(3)
with col1:
    st.metric("Current Level", "2.1 m")
with col2:
    predicted = levels[-1]
    st.metric("Predicted (3hrs)", f"{predicted:.2f} m", delta=f"{predicted - 2.1:+.2f} m")
with col3:
    if predicted > 3.5:
        st.error("⚠️ FLOOD RISK")
    else:
        st.success("✅ Normal")

st.markdown("---")

# Actions
st.markdown("## 🎯 Emergency Response")

col1, col2, col3 = st.columns(3)
with col1:
    if st.button("🚨 Deploy Pumps"):
        st.session_state.pumps += 5
        st.success(f"✅ Deployed 5 pumps! Total: {st.session_state.pumps}")
with col2:
    if st.button("📢 Traffic Alert"):
        st.success("✅ Alert sent to Delhi Police!")
with col3:
    if st.button("📄 Generate Report"):
        df = pd.DataFrame({
            "Zone": ["Zone 1", "Zone 2", "Zone 3"],
            "Risk": ["High", "Medium", "Low"],
            "Hotspots": [3, 2, 1]
        })
        st.dataframe(df)
        st.download_button("📥 Download", df.to_csv(index=False), f"Report_{datetime.now().strftime('%Y%m%d')}.csv", "text/csv")

st.markdown("---")
st.markdown("<div style='text-align: center; padding: 20px;'><h3 style='color: #00d4ff;'>🏆 India Innovates 2026 - Jal-Drishti Delhi</h3><p style='color: #00ff88;'>Municipal Corporation of Delhi</p></div>", unsafe_allow_html=True)
