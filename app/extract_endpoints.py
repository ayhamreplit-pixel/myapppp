import urllib.request
import re
import json

headers = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36',
    'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
    'Referer': 'https://ysscores.com/'
}

req = urllib.request.Request('https://ysscores.com/', headers=headers)
with urllib.request.urlopen(req, timeout=10) as resp:
    html = resp.read().decode('utf-8', errors='ignore')

js_files = re.findall(r'src="([^"]+\.js[^"]*)"', html)
endpoints = set()
image_patterns = set()

for js_url in js_files:
    if not js_url.startswith('http'):
        js_url = 'https://www.ysscores.com/' + js_url.lstrip('/')
    try:
        r = urllib.request.Request(js_url, headers=headers)
        with urllib.request.urlopen(r, timeout=6) as js_resp:
            content = js_resp.read().decode('utf-8', errors='ignore')
            found_api = re.findall(r'["\'](https?://[^"\']*api[^"\']*|/api/[^"\']+)["\']', content)
            for fa in found_api:
                endpoints.add(fa)
            found_imgs = re.findall(r'["\'](https?://imgs\.ysscores\.com/[^"\']+|[^"\']*imgs\.ysscores\.com[^"\']+)["\']', content)
            for fi in found_imgs:
                image_patterns.add(fi)
    except Exception as e:
        pass

print('--- FOUND API ENDPOINTS ---')
for ep in sorted(endpoints):
    print('  ', ep)

print('--- FOUND IMAGE PATTERNS ---')
for img in sorted(image_patterns):
    print('  ', img)
