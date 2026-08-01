Deploy this file to:
  - https://mercora.app/.well-known/assetlinks.json
  - https://mercora.netlify.app/.well-known/assetlinks.json

Fingerprint already filled: 17708ccb82f6b985a74b388a2ad874832fea3f0eb1713d1e5770b99b370bfaaa
(release keystore CN=Mercora, verified from signed APK via apksigner)

The file is also copied to merqora-web/public/.well-known/ and admin-web/public/.well-known/
so it ships with both deployments.

To regenerate if the keystore changes:
  keytool -list -v -keystore mercora_release.keystore -alias mercora | grep "SHA256:"
