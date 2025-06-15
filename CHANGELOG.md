# Astralis Mod - Changelog

## Version 1.0.0 - Translation and Fixes

### Fixed Issues
- **Fixed chunk loading in PlanetCommand**: Removed problematic `ChunkTicket` and `Thread.sleep()` approach, replaced with proper synchronous chunk loading for Minecraft 1.21.5
- **Fixed teleportation coordinates**: Now properly centers players and uses safe Y coordinates with fallback to sea level
- **Fixed gravity logic**: Corrected the gravity calculation in GravityManager to properly apply reduced gravity on low-gravity planets
- **Removed unused imports**: Cleaned up PlanetCommand imports

### Translation (Portuguese → English)
- **PlanetCommand**: All error messages and feedback translated to English
- **GravityManager**: All comments and documentation translated to English
- **EntityMixin**: Comment translation
- **fabric.mod.json**: Mod description translated to English

### Improvements
- **Enhanced teleportation safety**: Better coordinate calculation and safety checks
- **Improved gravity values**: Set realistic gravity values for Moon (0.16x) and Mars (0.38x)
- **Enabled custom gravity**: Custom gravity system is now active
- **Better error handling**: More descriptive error messages and logging
- **Java 21 compatibility**: Fixed Java toolchain issues for Minecraft 1.21.5

### Technical Details
- Updated gravity calculation to use proper physics simulation
- Improved chunk loading mechanism for modern Minecraft versions
- Added comprehensive logging for debugging
- Maintained server-side compatibility for vanilla clients

### Verified Features
✅ Planet teleportation (`/planet teleport astralis:mars`, `/planet teleport astralis:moon`)  
✅ Dimension creation and loading  
✅ Server-side operation  
✅ Custom gravity system (Moon: 16% gravity, Mars: 38% gravity)  
✅ Chunk loading and saving  

The mod is now fully functional and ready for use on Minecraft 1.21.5 servers.
