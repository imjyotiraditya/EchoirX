![Echoir Banner](.github/assets/banner.png)

[![API](https://img.shields.io/badge/26%2B-level?style=for-the-badge&logo=android&logoColor=3cd382&label=API&labelColor=21262d&color=ff663b)](https://developer.android.com/studio/releases/platforms) [![Language](https://img.shields.io/badge/Kotlin-language?style=for-the-badge&logo=kotlin&logoColor=7F52FF&label=language&labelColor=21262d&color=7F52FF)](https://kotlinlang.org/) [![Stars](https://img.shields.io/github/stars/imjyotiraditya/echoirx?style=for-the-badge&logo=github&labelColor=21262d&color=238636)](https://github.com/imjyotiraditya/echoirx/stargazers) [![Release](https://img.shields.io/github/v/release/imjyotiraditya/echoirx?display_name=tag&style=for-the-badge&logo=github&labelColor=21262d&color=1f6feb)](https://github.com/imjyotiraditya/echoirx/releases/latest) [![Downloads](https://img.shields.io/github/downloads/imjyotiraditya/echoirx/total?style=for-the-badge&labelColor=21262d&color=238636)](https://github.com/imjyotiraditya/echoirx/releases)

## Overview

Echoir (from Echo + Choir) is a sophisticated Material You music downloader for Android, delivering
high-quality audio formats while adhering to Material Design principles. The name cleverly plays on
the phrase "which will fall soon," reflecting the ephemeral nature of software projects.

## Key Features

### Audio Quality Options

| Quality Option          | Technical Details        |
|-------------------------|--------------------------|
| **Hi-Res Lossless**     | Up to 24-bit/192kHz FLAC |
| **CD Quality Lossless** | 16-bit/44.1kHz FLAC      |
| **Dolby Atmos**         | Enhanced AC-3/AC-4       |
| **High Quality AAC**    | 320kbps                  |
| **Standard Quality**    | 96kbps AAC               |

### Design & User Experience

- **Material You Theming** — Dynamic colors following system preferences
- **Material Design 3** — Modern components and animations
- **Intuitive Interface** — Simple workflow for downloading music

### Core Functionality

- **Background Processing** — Downloads continue with progress notifications
- **Comprehensive Metadata** — Extensive tag support for all major fields
- **Queue Management** — Organize and prioritize downloads
- **Customization Options**:
    - Configurable download locations
    - Flexible file naming formats
    - Smart filtering system

### Smart Filtering

- **Quality Filters:** Hi-Res, Lossless, Dolby Atmos
- **Content Filters:** Explicit, Clean

## Embedded Metadata

Echoir embeds rich metadata in every download:

| Category        | Details                                                                                       |
|-----------------|-----------------------------------------------------------------------------------------------|
| **Track Info**  | Title, Track Number, Total Tracks, Disc Number, Total Discs                                   |
| **Album Info**  | Album Title, Release Date, Year, Copyright, Record Label, UPC/Barcode                         |
| **Artist Info** | Artist(s), Album Artist, Composer, Lyricist, Producer, Mixer, Engineer, Mastering Engineer    |
| **Additional**  | Genre, Explicit Flag, ISRC, Cover Art (1280×1280), Synced Lyrics (when available), Source URL |

## Technical Foundation

Echoir represents a complete reimagining of its predecessor (FluidAC) with significant improvements:

- **Optimized Codebase** — Dramatically reduced APK size
- **Enhanced Functionality** — Background downloads and comprehensive metadata
- **Open Source** — Fully transparent development
- **Minimal Dependencies** — Custom-built minimal version of ffmpeg-kit

### Why FFmpeg?

Echoir utilizes a custom-built version of ffmpeg-kit specifically for Hi-Res Lossless content
processing:

- **Handles MPEG-DASH Segments** — Hi-Res content is delivered as segmented files
- **Stream Copy Only** — Uses `-c copy` flag for lossless concatenation without re-encoding
- **Format-Specific Processing** — Only used for Hi-Res content; other formats download directly

## Companion Projects

### Enhance Your Experience

- **Missing Lyrics?** Try [SongSync](https://github.com/Lambada10/SongSync/releases/latest) for
  managing and syncing lyrics

### Recommended Music Players

| App              | Description                    | Links                                            |
|------------------|--------------------------------|--------------------------------------------------|
| **Gramophone**   | Modern offline player with MD3 | [GitHub](https://github.com/AkaneTan/Gramophone) |
| **AccordLegacy** | Apple Music style player       | [Telegram](https://t.me/light_summer)            |

## Credits

- **Design:**
    - App launcher icon and banner by [Xelxen](https://github.com/Xelxen)
    - UI design improvements based on Figma mockups
      by [HotarunIchijou](https://github.com/HotarunIchijou)
- **Core Technology:** [ffmpeg-kit](https://github.com/arthenica/ffmpeg-kit) by Arthenica (custom
  minimal build)
