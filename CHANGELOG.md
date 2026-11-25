# WTM v1.0.5

- Fixed welcome, first-join, broadcast and individual title timing API (`Title.Times.times` instead of removed `of`).
- Preserved the large five-line colored DVSH banner; added font-safe ASCII rendering for web consoles.
- Added `startup-banner.style: ascii | block` (ASCII by default).
- Maven compilation target updated to Paper API 26.3 build 262 and Java 25.
- All existing commands, listeners and configuration options remain in place.

Build and test against your actual Paper 26.3 installation before publishing a GitHub release.
