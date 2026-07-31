Deploy this file to:
  - https://mercora.app/.well-known/assetlinks.json
  - https://mercora.netlify.app/.well-known/assetlinks.json

Replace SHA256_CERT_FINGERPRINT with your actual release certificate fingerprint:
  keytool -list -v -keystore mercora_release.keystore -alias mercora_key 2>/dev/null | grep "SHA256:" | awk '{print $2}'
