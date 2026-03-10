# 🔧 Troubleshooting Guide - Jal-Drishti Delhi

## Common Issues and Solutions

### ❌ Issue: "No space left on device" Error

**Problem:** Your system disk is full and cannot install the required packages.

**Solutions:**

#### Option 1: Free Up Disk Space (Recommended)

1. **Clean Temporary Files:**
```bash
# Run Disk Cleanup
cleanmgr

# Or manually delete temp files from:
C:\Users\HP\AppData\Local\Temp
C:\Windows\Temp
```

2. **Clear Python Cache:**
```bash
pip cache purge
```

3. **Remove Unused Programs:**
- Go to Settings → Apps → Apps & features
- Uninstall programs you don't need

4. **Empty Recycle Bin**

#### Option 2: Install on Different Drive

If you have another drive with more space:

```bash
# Set pip to use different cache directory
pip install --cache-dir D:\pip_cache streamlit-folium folium
```

#### Option 3: Use Lite Version (No Map)

We can modify the app to work without the map feature temporarily:

1. Rename `app.py` to `app_full.py`
2. Create a simplified version without folium
3. Install only: `streamlit plotly pandas numpy Pillow`

---

### ❌ Issue: Module Not Found Error

**Error:** `ModuleNotFoundError: No module named 'streamlit_folium'`

**Solution:**
```bash
pip install streamlit-folium folium
```

If disk space is the issue, see "No space left on device" above.

---

### ❌ Issue: App Won't Start

**Symptoms:** `streamlit run app.py` shows errors

**Solutions:**

1. **Check Python Version:**
```bash
python --version
# Should be 3.8 or higher
```

2. **Verify All Packages:**
```bash
pip list | findstr "streamlit folium plotly pandas numpy Pillow"
```

3. **Reinstall Streamlit:**
```bash
pip uninstall streamlit
pip install streamlit
```

---

### ❌ Issue: Port Already in Use

**Error:** `Address already in use`

**Solution:**
```bash
# Use a different port
streamlit run app.py --server.port=8502

# Or find and kill the process using port 8501
netstat -ano | findstr :8501
taskkill /PID <PID_NUMBER> /F
```

---

### ❌ Issue: Map Not Displaying

**Symptoms:** Map shows blank or loading forever

**Solutions:**

1. **Check Internet Connection:**
   - The map requires internet to load OpenStreetMap tiles

2. **Clear Browser Cache:**
   - Press `Ctrl + Shift + Delete`
   - Clear cached images and files

3. **Try Different Browser:**
   - Chrome, Firefox, or Edge

4. **Check Folium Installation:**
```bash
python -c "import folium; print(folium.__version__)"
```

---

### ❌ Issue: Slow Performance

**Solutions:**

1. **Close Other Applications:**
   - Free up RAM

2. **Reduce Rainfall Frequency:**
   - Don't move slider too quickly

3. **Disable Auto-refresh:**
   - Uncheck "Auto-refresh" in sidebar

---

## 🚀 Quick Fixes

### Minimal Installation (If Space is Critical)

```bash
# Install only essential packages
pip install streamlit plotly pandas numpy
```

Then use this simplified app (without map):

```python
# Save as app_simple.py
import streamlit as st
import plotly.graph_objects as go

st.title("🌊 Jal-Drishti Delhi - Lite Version")
st.info("Map feature disabled due to disk space. Install 'streamlit-folium' for full version.")

# Rest of the app without map features...
```

---

## 🆘 Still Having Issues?

1. **Check System Requirements:**
   - Python 3.8+
   - 2 GB free disk space
   - 4 GB RAM minimum
   - Internet connection

2. **Verify Installation:**
```bash
cd "c:\Users\HP\OneDrive\Desktop\Jal-Drishti Delhi"
python -m streamlit --version
```

3. **Run in Safe Mode:**
```bash
streamlit run app.py --server.headless=true
```

4. **Check Logs:**
```bash
# Streamlit logs are in:
C:\Users\HP\.streamlit\logs
```

---

## 💡 Pro Tips

### Virtual Environment (Recommended)

Avoid conflicts with other Python projects:

```bash
# Create virtual environment
python -m venv venv

# Activate it
venv\Scripts\activate

# Install packages
pip install -r requirements.txt

# Run app
streamlit run app.py
```

### Docker Alternative

If all else fails, use Docker:

```bash
docker build -t jal-drishti .
docker run -p 8501:8501 jal-drishti
```

---

## 📞 Getting Help

If none of these solutions work:

1. **Check GitHub Issues** (if this is on GitHub)
2. **Streamlit Community Forum:** https://discuss.streamlit.io/
3. **Stack Overflow:** Tag with `streamlit` and `python`

---

## ✅ Verification Checklist

Before running the app, verify:

- [ ] Python 3.8+ installed
- [ ] At least 2 GB free disk space
- [ ] Internet connection active
- [ ] All packages installed: `pip list`
- [ ] In correct directory: `cd "Jal-Drishti Delhi"`
- [ ] No other Streamlit apps running on port 8501

---

**Last Updated:** March 2026  
**For:** India Innovates 2026 Hackathon
