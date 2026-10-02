import base64

# Simple valid 1x1 loading shimmer GIF in base64
raw_gif_b64 = (
    "R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7"
)

# Decode GIF
gif_data = base64.b64decode(raw_gif_b64)

with open("/app/src/main/res/drawable/loading_skeleton.gif", "wb") as f:
    f.write(gif_data)

print("GIF created successfully!")
