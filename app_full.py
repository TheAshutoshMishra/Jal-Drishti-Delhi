import streamlit as st
import plotly.graph_objects as go
import pandas as pd
import numpy as np
from datetime import datetime, timedelta

# Page config
st.set_page_config(
    page_title="MCD Flood Command Center", 
    page_icon="💧", 
    layout="wide",
    menu_items={
        'Get Help': None,
        'Report a bug': None,
        'About': None
    }
)

# Custom CSS
st.markdown("""
<style>
.stApp { background: linear-gradient(135deg, #0f0c29, #302b63, #24243e); }
h1 { color: #00d4ff !important; text-align: center; text-shadow: 0 0 20px #00d4ff; }
h2, h3 { color: #00ff88 !important; }
/* Hide only Deploy button */
[data-testid="stToolbar"] button[kind="header"] { display: none; }
footer { visibility: hidden; }
.big-card {
    background: linear-gradient(135deg, rgba(0, 212, 255, 0.1), rgba(0, 255, 136, 0.1));
    padding: 30px;
    border-radius: 15px;
    border: 2px solid #00d4ff;
    margin: 20px 0;
    cursor: pointer;
    transition: all 0.3s;
}
.big-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 10px 30px rgba(0, 212, 255, 0.5);
    border-color: #00ff88;
}
.stat-box {
    background: rgba(0, 0, 0, 0.4);
    padding: 20px;
    border-radius: 10px;
    border-left: 4px solid #00d4ff;
    margin: 10px 0;
}
</style>
""", unsafe_allow_html=True)

# Session state
if 'rainfall' not in st.session_state:
    st.session_state.rainfall = 0
if 'pumps' not in st.session_state:
    st.session_state.pumps = 0
if 'page' not in st.session_state:
    st.session_state.page = 'home'

# Navigation function
def navigate_to(page):
    st.session_state.page = page
    st.rerun()

# Sidebar navigation
with st.sidebar:
    st.markdown("# Navigation")
    st.markdown("---")
    
    if st.button("Home Dashboard", use_container_width=True):
        navigate_to('home')
    if st.button("Flood Hotspots Details", use_container_width=True):
        navigate_to('hotspots')
    if st.button("Drainage Network", use_container_width=True):
        navigate_to('drainage')
    if st.button("Analytics & Predictions", use_container_width=True):
        navigate_to('analytics')
    if st.button("Historical Data", use_container_width=True):
        navigate_to('historical')
    if st.button("Emergency Actions", use_container_width=True):
        navigate_to('emergency')
    
    st.markdown("---")
    st.markdown("### Controls")
    rainfall = st.slider("Rainfall (mm)", 0, 150, st.session_state.rainfall, 5)
    st.session_state.rainfall = rainfall
    
    st.markdown("---")
    st.markdown("### Live Stats")
    st.metric("Rainfall", f"{rainfall} mm")
    st.metric("Pumps", st.session_state.pumps)
    alerts = min(int(rainfall / 15), 10)
    st.metric("Alerts", alerts)

# ==================== HOME PAGE ====================
if st.session_state.page == 'home':
    st.markdown("# Jal-Drishti Delhi")
    st.markdown("### Team Zenyukti | Real-Time Flood Intelligence System")
    st.markdown("---")
    
    # Top metrics
    col1, col2, col3, col4 = st.columns(4)
    with col1:
        st.metric("Total Rainfall (24h)", f"{rainfall * 0.6:.1f} mm", delta=f"+{rainfall * 0.1:.1f}")
    with col2:
        st.metric("Active Alerts", alerts, delta=f"+{max(0, alerts-2)}")
    with col3:
        st.metric("Pumps Active", st.session_state.pumps)
    with col4:
        st.metric("Drains Clear", "15/22", delta="68%")
    
    st.markdown("---")
    
    # Quick access cards
    st.markdown("## Quick Access Modules")
    
    col1, col2 = st.columns(2)
    
    with col1:
        if st.button("**View All Flood Hotspots**\n\n10 Critical Zones Monitored", use_container_width=True, key="btn1"):
            navigate_to('hotspots')
        
        st.markdown("""
        <div class='stat-box'>
            <h3>Current Risk Status</h3>
            <p>▪ <b>Critical Zones:</b> 4</p>
            <p>▪ <b>Moderate Risk:</b> 3</p>
            <p>▪ <b>Safe Zones:</b> 3</p>
        </div>
        """, unsafe_allow_html=True)
    
    with col2:
        if st.button("**Drainage Network Monitor**\n\n2,152 km Network Coverage", use_container_width=True, key="btn2"):
            navigate_to('drainage')
        
        st.markdown("""
        <div class='stat-box'>
            <h3>Drainage Health</h3>
            <p>▪ <b>Operational:</b> 15 Drains</p>
            <p>▪ <b>Needs Cleaning:</b> 5 Drains</p>
            <p>▪ <b>Blocked:</b> 2 Drains</p>
        </div>
        """, unsafe_allow_html=True)
    
    col1, col2 = st.columns(2)
    
    with col1:
        if st.button("**AI Predictions & Analytics**\n\nLSTM 6-Hour Forecast", use_container_width=True, key="btn3"):
            navigate_to('analytics')
    
    with col2:
        if st.button("**Historical Flood Data**\n\n10 Years of Data", use_container_width=True, key="btn4"):
            navigate_to('historical')
    
    st.markdown("---")
    
    # Recent alerts
    st.markdown("## Recent Alerts (Last 2 Hours)")
    
    alerts_data = pd.DataFrame({
        "Time": ["17:45", "17:30", "17:15", "17:00"],
        "Location": ["Minto Road", "Najafgarh", "ITO", "Kirari"],
        "Type": ["Water Level Rising", "Heavy Rainfall", "Drain Overflow", "Traffic Alert"],
        "Severity": ["Critical", "Moderate", "Critical", "Moderate"]
    })
    st.dataframe(alerts_data, use_container_width=True, hide_index=True)
    
    st.markdown("---")
    
    # Live prediction preview
    st.markdown("## Live Water Level Prediction")
    
    current_time = datetime.now()
    times = [current_time + timedelta(hours=i) for i in range(-2, 4)]
    levels = [2.1 - 0.3, 2.1 - 0.15, 2.1] + [2.1 + i * (rainfall / 50) * 0.3 for i in range(1, 4)]
    
    fig = go.Figure()
    fig.add_trace(go.Scatter(x=times[:3], y=levels[:3], mode='lines+markers', name='Historical', 
                             line=dict(color='#00d4ff', width=4), marker=dict(size=12)))
    fig.add_trace(go.Scatter(x=times[2:], y=levels[2:], mode='lines+markers', name='Predicted', 
                             line=dict(color='#ff006e', width=4, dash='dash'), marker=dict(size=12)))
    fig.add_hline(y=3.5, line_dash="dot", line_color="red", annotation_text="Danger Level")
    fig.update_layout(template="plotly_dark", height=400, paper_bgcolor='rgba(0,0,0,0)', 
                      plot_bgcolor='rgba(0,0,0,0.2)', font=dict(size=14))
    
    st.plotly_chart(fig, use_container_width=True, key="home_prediction")

# ==================== HOTSPOTS PAGE ====================
elif st.session_state.page == 'hotspots':
    st.markdown("# Delhi Flood Hotspots - Detailed View")
    st.markdown("### 10 High-Risk Micro-Zones Under 24/7 Monitoring")
    st.markdown("---")
    
    hotspots = [
        {"name": "Minto Road", "lat": 28.6289, "lon": 77.2334, "elevation": 212, "risk": 0.85, "population": "50,000", "area": "2.3 km²"},
        {"name": "Kashmere Gate", "lat": 28.6692, "lon": 77.2289, "elevation": 215, "risk": 0.78, "population": "35,000", "area": "1.8 km²"},
        {"name": "Kirari", "lat": 28.7747, "lon": 77.0371, "elevation": 208, "risk": 0.92, "population": "75,000", "area": "4.1 km²"},
        {"name": "ITO", "lat": 28.6289, "lon": 77.2497, "elevation": 211, "risk": 0.88, "population": "45,000", "area": "2.0 km²"},
        {"name": "Najafgarh", "lat": 28.6092, "lon": 76.9798, "elevation": 205, "risk": 0.95, "population": "90,000", "area": "5.2 km²"},
        {"name": "Yamuna Bank", "lat": 28.6414, "lon": 77.2833, "elevation": 209, "risk": 0.82, "population": "40,000", "area": "1.5 km²"},
        {"name": "Safdarjung", "lat": 28.5562, "lon": 77.1901, "elevation": 216, "risk": 0.71, "population": "30,000", "area": "1.2 km²"},
        {"name": "Pul Prahladpur", "lat": 28.4877, "lon": 77.2892, "elevation": 207, "risk": 0.89, "population": "55,000", "area": "2.8 km²"},
        {"name": "Gandhi Nagar", "lat": 28.6631, "lon": 77.2464, "elevation": 213, "risk": 0.76, "population": "38,000", "area": "1.6 km²"},
        {"name": "Tilak Nagar", "lat": 28.6414, "lon": 77.0955, "elevation": 210, "risk": 0.84, "population": "42,000", "area": "2.1 km²"},
    ]
    
    # Summary stats
    col1, col2, col3, col4 = st.columns(4)
    with col1:
        st.metric("Total Zones", "10")
    with col2:
        critical = sum(1 for h in hotspots if h['risk'] * (1 + rainfall/100) > 0.75)
        st.metric("Critical", critical)
    with col3:
        st.metric("Population at Risk", "500K+")
    with col4:
        st.metric("Total Area", "24.6 km²")
    
    st.markdown("---")
    
    # Detailed cards for each hotspot
    for idx, spot in enumerate(hotspots):
        risk = min(spot['risk'] * (1 + rainfall / 100), 1.0)
        status = "CRITICAL" if risk > 0.75 else "MODERATE" if risk > 0.5 else "SAFE"
        color = "#ff0000" if risk > 0.75 else "#ffaa00" if risk > 0.5 else "#00ff00"
        
        with st.expander(f"**{spot['name']}** - {status} ({risk*100:.1f}% Risk)", expanded=(idx == 0)):
            col1, col2, col3 = st.columns(3)
            
            with col1:
                st.markdown(f"""
                <div class='stat-box'>
                    <h4>Location Details</h4>
                    <p>▪ <b>Coordinates:</b> {spot['lat']:.4f}, {spot['lon']:.4f}</p>
                    <p>▪ <b>Elevation:</b> {spot['elevation']} meters</p>
                    <p>▪ <b>Area:</b> {spot['area']}</p>
                </div>
                """, unsafe_allow_html=True)
            
            with col2:
                st.markdown(f"""
                <div class='stat-box'>
                    <h4>Demographics</h4>
                    <p>▪ <b>Population:</b> {spot['population']}</p>
                    <p>▪ <b>Households:</b> {int(spot['population'].replace(',','')) // 5:,}</p>
                    <p>▪ <b>Density:</b> High</p>
                </div>
                """, unsafe_allow_html=True)
            
            with col3:
                st.markdown(f"""
                <div class='stat-box' style='border-left-color: {color};'>
                    <h4>Risk Assessment</h4>
                    <p>▪ <b>Base Risk:</b> {spot['risk']*100:.1f}%</p>
                    <p>▪ <b>Current Risk:</b> {risk*100:.1f}%</p>
                    <p>▪ <b>Status:</b> {status}</p>
                </div>
                """, unsafe_allow_html=True)
            
            # Risk trend chart
            hours = list(range(-6, 1))
            risk_history = [min(spot['risk'] * (1 + (rainfall - i*5) / 100), 1.0) * 100 for i in reversed(range(7))]
            
            fig = go.Figure()
            fig.add_trace(go.Scatter(x=hours, y=risk_history, mode='lines+markers', 
                                    fill='tozeroy', line=dict(color=color, width=3)))
            fig.update_layout(title=f"Risk Trend (Last 6 Hours)", template="plotly_dark", height=250,
                             yaxis_title="Risk %", xaxis_title="Hours Ago", 
                             paper_bgcolor='rgba(0,0,0,0)', plot_bgcolor='rgba(0,0,0,0.2)')
            st.plotly_chart(fig, use_container_width=True, key=f"hotspot_chart_{idx}")
            
            # Action buttons
            col1, col2, col3 = st.columns(3)
            with col1:
                if st.button(f"Deploy Pumps", key=f"pump_{idx}"):
                    st.success(f"Deployed 2 pumps to {spot['name']}")
            with col2:
                if st.button(f"Traffic Diversion", key=f"traffic_{idx}"):
                    st.success(f"Traffic alert sent for {spot['name']}")
            with col3:
                if st.button(f"Alert Residents", key=f"alert_{idx}"):
                    st.success(f"SMS alert sent to {spot['population']} residents")

# ==================== DRAINAGE PAGE ====================
elif st.session_state.page == 'drainage':
    st.markdown("# MCD Drainage Network Monitoring")
    st.markdown("### AI-Powered 24/7 Surveillance of 2,152 km Network")
    st.markdown("---")
    
    # Network stats
    col1, col2, col3, col4 = st.columns(4)
    with col1:
        st.metric("Total Network", "2,152 km")
    with col2:
        st.metric("Operational", "15 Drains", delta="68%")
    with col3:
        st.metric("Needs Maintenance", "5 Drains", delta="23%")
    with col4:
        st.metric("Blocked", "2 Drains", delta="9%")
    
    st.markdown("---")
    
    drains = [
        {"name": "Najafgarh Drain", "length": "51 km", "status": "Silted", "confidence": 87.3, "capacity": "68%", "flow": "245 m³/s", "last_cleaned": "2 months ago"},
        {"name": "Barapullah Drain", "length": "28 km", "status": "Clear", "confidence": 94.1, "capacity": "92%", "flow": "180 m³/s", "last_cleaned": "1 week ago"},
        {"name": "Gazipur Drain", "length": "22 km", "status": "Blocked", "confidence": 91.8, "capacity": "34%", "flow": "85 m³/s", "last_cleaned": "4 months ago"},
        {"name": "Shahdara Drain", "length": "18 km", "status": "Clear", "confidence": 89.5, "capacity": "88%", "flow": "160 m³/s", "last_cleaned": "2 weeks ago"},
        {"name": "Supplementary Drain", "length": "14 km", "status": "Silted", "confidence": 76.2, "capacity": "61%", "flow": "120 m³/s", "last_cleaned": "3 months ago"},
        {"name": "Mori Gate Drain", "length": "12 km", "status": "Clear", "confidence": 92.7, "capacity": "85%", "flow": "95 m³/s", "last_cleaned": "10 days ago"},
    ]
    
    for idx, drain in enumerate(drains):
        status_color = {"Clear": "#00ff00", "Silted": "#ffaa00", "Blocked": "#ff0000"}
        
        with st.expander(f"**{drain['name']}** ({drain['length']}) - {drain['status']}", expanded=(idx < 2)):
            col1, col2 = st.columns([2, 1])
            
            with col1:
                # Mock CCTV visualization
                st.markdown(f"""
                <div style='background: #1a1a2e; padding: 20px; border-radius: 10px; border: 2px solid {status_color[drain['status']]};'>
                    <h4>Live CCTV Feed</h4>
                    <p style='color: {status_color[drain['status']]}; font-size: 24px; text-align: center; padding: 40px;'>
                        ● LIVE - {drain['status'].upper()}
                    </p>
                    <p style='font-size: 12px; opacity: 0.7;'>Last Update: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}</p>
                </div>
                """, unsafe_allow_html=True)
            
            with col2:
                st.markdown(f"""
                <div class='stat-box' style='border-left-color: {status_color[drain['status']]};'>
                    <h4>Status</h4>
                    <p>▪ <b>AI Confidence:</b> {drain['confidence']}%</p>
                    <p>▪ <b>Capacity:</b> {drain['capacity']}</p>
                    <p>▪ <b>Flow Rate:</b> {drain['flow']}</p>
                    <p>▪ <b>Last Cleaned:</b> {drain['last_cleaned']}</p>
                </div>
                """, unsafe_allow_html=True)
            
            # Flow rate trend
            hours = list(range(24))
            base_flow = float(drain['flow'].split()[0])
            flow_data = [base_flow + np.random.uniform(-20, 20) for _ in hours]
            
            fig = go.Figure()
            fig.add_trace(go.Scatter(x=hours, y=flow_data, mode='lines', fill='tozeroy',
                                    line=dict(color=status_color[drain['status']], width=2)))
            fig.update_layout(title="Flow Rate (Last 24 Hours)", template="plotly_dark", height=200,
                             yaxis_title="Flow (m³/s)", xaxis_title="Hours", 
                             paper_bgcolor='rgba(0,0,0,0)', plot_bgcolor='rgba(0,0,0,0.2)')
            st.plotly_chart(fig, use_container_width=True, key=f"drain_chart_{idx}")
            
            # Actions
            col1, col2, col3 = st.columns(3)
            with col1:
                if drain['status'] != "Clear":
                    if st.button(f"Schedule Desilting", key=f"desilt_{idx}"):
                        st.success(f"Maintenance crew dispatched to {drain['name']}")
            with col2:
                if st.button(f"View Full Report", key=f"report_{idx}"):
                    st.info(f"Generating detailed report for {drain['name']}...")
            with col3:
                if st.button(f"Set Alert", key=f"setalert_{idx}"):
                    st.success(f"Alert configured for {drain['name']}")

# ==================== ANALYTICS PAGE ====================
elif st.session_state.page == 'analytics':
    st.markdown("# AI Predictions & Analytics Dashboard")
    st.markdown("### LSTM-Based Water Level Forecasting & Risk Analysis")
    st.markdown("---")
    
    # Prediction metrics
    col1, col2, col3, col4 = st.columns(4)
    current_level = 2.1
    predicted_level = current_level + (rainfall / 50) * 0.9
    
    with col1:
        st.metric("Current Level", f"{current_level:.2f} m")
    with col2:
        st.metric("Predicted (3h)", f"{predicted_level:.2f} m", delta=f"{predicted_level - current_level:+.2f} m")
    with col3:
        accuracy = 94.5 - (rainfall * 0.1)
        st.metric("Model Accuracy", f"{accuracy:.1f}%")
    with col4:
        confidence = 91.2 - (rainfall * 0.08)
        st.metric("Confidence", f"{confidence:.1f}%")
    
    st.markdown("---")
    
    # Main prediction graph
    st.markdown("## 6-Hour Water Level Forecast (LSTM)")
    
    current_time = datetime.now()
    times = [current_time + timedelta(hours=i) for i in range(-6, 7)]
    
    # Generate realistic data
    historical = [2.1 - 0.6 + i * 0.1 + np.random.uniform(-0.05, 0.05) for i in range(7)]
    predicted = [historical[-1]]
    for i in range(1, 7):
        next_val = predicted[-1] + (rainfall / 50) * 0.15 + np.random.uniform(-0.1, 0.1)
        predicted.append(next_val)
    
    all_levels = historical + predicted[1:]
    
    fig = go.Figure()
    fig.add_trace(go.Scatter(x=times[:7], y=historical, mode='lines+markers', name='Historical Data',
                             line=dict(color='#00d4ff', width=4), marker=dict(size=10)))
    fig.add_trace(go.Scatter(x=times[6:], y=predicted, mode='lines+markers', name='LSTM Prediction',
                             line=dict(color='#ff006e', width=4, dash='dash'), marker=dict(size=10, symbol='diamond')))
    fig.add_hline(y=3.5, line_dash="dot", line_color="red", annotation_text="Danger Level (3.5m)")
    fig.add_hline(y=3.0, line_dash="dot", line_color="orange", annotation_text="Warning Level (3.0m)")
    
    fig.update_layout(template="plotly_dark", height=500, 
                      title="Water Level Prediction with Confidence Intervals",
                      yaxis_title="Water Level (meters)", xaxis_title="Time",
                      paper_bgcolor='rgba(0,0,0,0)', plot_bgcolor='rgba(0,0,0,0.2)',
                      font=dict(size=14))
    
    st.plotly_chart(fig, use_container_width=True, key="analytics_main_pred")
    
    # Risk distribution
    st.markdown("## Risk Distribution Across Zones")
    
    zones = ["North", "South", "East", "West", "Central"]
    risk_levels = [min(0.9 * (1 + rainfall/100), 1), min(0.7 * (1 + rainfall/100), 1), 
                   min(0.85 * (1 + rainfall/100), 1), min(0.75 * (1 + rainfall/100), 1),
                   min(0.8 * (1 + rainfall/100), 1)]
    
    fig = go.Figure(data=[go.Bar(x=zones, y=risk_levels, marker_color=['#ff0000' if r > 0.75 else '#ffaa00' if r > 0.5 else '#00ff00' for r in risk_levels])])
    fig.update_layout(template="plotly_dark", height=350, title="Current Risk by Zone",
                      yaxis_title="Risk Level", paper_bgcolor='rgba(0,0,0,0)', plot_bgcolor='rgba(0,0,0,0.2)')
    st.plotly_chart(fig, use_container_width=True, key="analytics_risk_dist")
    
    # Rainfall correlation
    st.markdown("## Rainfall vs Flood Incidents (Historical)")
    
    months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"]
    rainfall_data = [20, 30, 25, 15, 40, 120, 180, 200, 150, 80, 30, 15]
    incidents = [2, 3, 2, 1, 5, 15, 25, 30, 20, 10, 3, 1]
    
    fig = go.Figure()
    fig.add_trace(go.Bar(x=months, y=rainfall_data, name='Rainfall (mm)', marker_color='#00d4ff'))
    fig.add_trace(go.Scatter(x=months, y=incidents, name='Flood Incidents', mode='lines+markers',
                             line=dict(color='#ff006e', width=3), marker=dict(size=10), yaxis='y2'))
    
    fig.update_layout(template="plotly_dark", height=400,
                      yaxis=dict(title="Rainfall (mm)"),
                      yaxis2=dict(title="Incidents", overlaying='y', side='right'),
                      paper_bgcolor='rgba(0,0,0,0)', plot_bgcolor='rgba(0,0,0,0.2)')
    st.plotly_chart(fig, use_container_width=True, key="analytics_rainfall_corr")

# ==================== HISTORICAL PAGE ====================
elif st.session_state.page == 'historical':
    st.markdown("# Historical Flood Data Analysis")
    st.markdown("### 10 Years of Comprehensive Data (2016-2026)")
    st.markdown("---")
    
    # Summary stats
    col1, col2, col3, col4 = st.columns(4)
    with col1:
        st.metric("Years Analyzed", "10")
    with col2:
        st.metric("Total Incidents", "347")
    with col3:
        st.metric("People Affected", "2.3M")
    with col4:
        st.metric("Damage Cost", "₹450 Cr")
    
    st.markdown("---")
    
    # Yearly trends
    st.markdown("## Flood Incidents by Year")
    
    years = list(range(2016, 2027))
    incidents_per_year = [25, 30, 28, 32, 40, 35, 38, 42, 35, 28, 14]
    
    fig = go.Figure()
    fig.add_trace(go.Scatter(x=years, y=incidents_per_year, mode='lines+markers',
                             fill='tozeroy', line=dict(color='#00d4ff', width=4),
                             marker=dict(size=12, color=incidents_per_year, colorscale='Reds', showscale=True)))
    fig.update_layout(template="plotly_dark", height=400, title="Historical Trend Analysis",
                      yaxis_title="Number of Incidents", xaxis_title="Year",
                      paper_bgcolor='rgba(0,0,0,0)', plot_bgcolor='rgba(0,0,0,0.2)')
    st.plotly_chart(fig, use_container_width=True, key="historical_yearly")
    
    # Monthly distribution
    st.markdown("## Seasonal Distribution")
    
    months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"]
    avg_incidents = [3, 4, 3, 2, 6, 18, 32, 38, 25, 12, 4, 2]
    
    fig = go.Figure(data=[go.Bar(x=months, y=avg_incidents, 
                                  marker_color=['#00ff00' if i < 5 else '#ffaa00' if i < 15 else '#ff0000' for i in avg_incidents])])
    fig.update_layout(template="plotly_dark", height=350, title="Average Monthly Incidents (2016-2026)",
                      yaxis_title="Incidents", paper_bgcolor='rgba(0,0,0,0)', plot_bgcolor='rgba(0,0,0,0.2)')
    st.plotly_chart(fig, use_container_width=True, key="historical_monthly")
    
    # Top affected areas
    st.markdown("## Most Affected Areas (2016-2026)")
    
    affected_data = pd.DataFrame({
        "Area": ["Najafgarh", "Kirari", "ITO", "Minto Road", "Pul Prahladpur", "Yamuna Bank"],
        "Incidents": [45, 38, 35, 32, 28, 25],
        "Avg Depth (m)": [1.8, 1.6, 1.5, 1.7, 1.4, 1.3],
        "People Affected": ["450K", "380K", "350K", "320K", "280K", "250K"]
    })
    
    st.dataframe(affected_data, use_container_width=True, hide_index=True)
    
    # Download historical data
    if st.button("Download Complete Historical Dataset (CSV)"):
        full_data = pd.DataFrame({
            "Year": years * 12,
            "Month": [m for m in range(1, 13) for _ in years],
            "Incidents": [np.random.randint(0, 5) if m < 6 or m > 9 else np.random.randint(10, 40) for _ in years for m in range(1, 13)],
            "Rainfall": [np.random.randint(10, 50) if m < 6 or m > 9 else np.random.randint(100, 250) for _ in years for m in range(1, 13)]
        })
        csv = full_data.to_csv(index=False)
        st.download_button("Download CSV", csv, f"MCD_Flood_Data_2016-2026.csv", "text/csv")

# ==================== EMERGENCY PAGE ====================
elif st.session_state.page == 'emergency':
    st.markdown("# Emergency Response Command Panel")
    st.markdown("### One-Click Disaster Management Actions")
    st.markdown("---")
    
    # Status overview
    col1, col2, col3 = st.columns(3)
    with col1:
        st.markdown("""
        <div class='stat-box' style='border-left-color: #00d4ff;'>
            <h3>Response Status</h3>
            <p>▪ <b>Teams Available:</b> 12</p>
            <p>▪ <b>Pumps Ready:</b> 45</p>
            <p>▪ <b>Response Time:</b> <15 min</p>
        </div>
        """, unsafe_allow_html=True)
    with col2:
        st.markdown("""
        <div class='stat-box' style='border-left-color: #00ff88;'>
            <h3>Equipment</h3>
            <p>▪ <b>Pumps Active:</b> {}</p>
            <p>▪ <b>Boats Ready:</b> 8</p>
            <p>▪ <b>Rescue Teams:</b> 15</p>
        </div>
        """.format(st.session_state.pumps), unsafe_allow_html=True)
    with col3:
        st.markdown("""
        <div class='stat-box' style='border-left-color: #ff006e;'>
            <h3>Coordination</h3>
            <p>▪ <b>Police Connected:</b> Yes</p>
            <p>▪ <b>Fire Dept:</b> Yes</p>
            <p>▪ <b>Hospitals:</b> Yes</p>
        </div>
        """, unsafe_allow_html=True)
    
    st.markdown("---")
    st.markdown("## Quick Actions")
    
    col1, col2 = st.columns(2)
    
    with col1:
        if st.button("Deploy Emergency Pumps (x5)", use_container_width=True, type="primary"):
            st.session_state.pumps += 5
            st.success(f"Deployed 5 pumps! Total active: {st.session_state.pumps}")
            st.balloons()
        
        if st.button("Activate Traffic Diversion Protocol", use_container_width=True):
            st.success("Traffic alert sent to Delhi Police HQ!")
            st.info("""
            **Routes Diverted:**
            - Minto Road → ITO Bypass
            - Ring Road → Outer Ring Road
            - NH-8 → Alternative Route
            """)
        
        if st.button("Send Mass SMS Alert", use_container_width=True):
            st.success("SMS sent to 500,000 residents in affected areas!")
            st.info("Message: 'Heavy rain alert. Avoid low-lying areas. Stay indoors. MCD Helpline: 1800-XXX-XXXX'")
        
        if st.button("Request NDRF Deployment", use_container_width=True):
            st.warning("NDRF deployment requested. ETA: 45 minutes")
    
    with col2:
        if st.button("Alert All Emergency Services", use_container_width=True, type="primary"):
            st.success("Coordinated alert sent!")
            st.info("""
            **Notified:**
            - Delhi Police
            - Fire Department
            - Hospitals
            - PWD
            - Media
            """)
        
        if st.button("Evacuate High-Risk Zones", use_container_width=True):
            st.error("Evacuation order issued for:")
            st.info("- Najafgarh (90,000 people)\n- Kirari (75,000 people)\n- Minto Road (50,000 people)")
        
        if st.button("Mobilize Medical Teams", use_container_width=True):
            st.success("8 mobile medical units dispatched!")
        
        if st.button("Generate Situation Report", use_container_width=True):
            report_data = pd.DataFrame({
                "Zone": ["North", "South", "East", "West", "Central"],
                "Status": ["Critical", "Safe", "Moderate", "Moderate", "Critical"],
                "Pumps": [8, 0, 3, 3, 6],
                "People Evacuated": [5000, 0, 1200, 800, 3500]
            })
            st.dataframe(report_data, use_container_width=True, hide_index=True)
            
            csv = report_data.to_csv(index=False)
            st.download_button("Download Full Report", csv, 
                             f"Emergency_Report_{datetime.now().strftime('%Y%m%d_%H%M')}.csv", "text/csv")
    
    st.markdown("---")
    st.markdown("## Emergency Contacts")
    
    col1, col2, col3 = st.columns(3)
    with col1:
        st.info("**MCD Control Room**\n1800-XXX-XXXX")
    with col2:
        st.info("**Delhi Police**\n100 / 112")
    with col3:
        st.info("**NDRF**\n9711-077-372")

# Footer
st.markdown("---")
st.markdown("""
<div style='text-align: center; padding: 20px; background: rgba(0,0,0,0.3); border-radius: 10px;'>
    <h3 style='color: #00d4ff;'>Jal-Drishti Delhi</h3>
    <p style='color: #00ff88;'>Team Zenyukti</p>
</div>
""", unsafe_allow_html=True)
