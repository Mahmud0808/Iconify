# Iconify

<p align="center">
  <a href="https://github.com/Mahmud0808/Iconify/actions"><img src="https://img.shields.io/github/actions/workflow/status/Mahmud0808/Iconify/build_debug.yml?branch=beta&label=Debug%20Build&style=for-the-badge" alt="Debug Build"></a>
  <a href="https://github.com/Mahmud0808/Iconify"><img alt="Repo Size" src="https://img.shields.io/github/repo-size/Mahmud0808/Iconify?style=for-the-badge"></a>
  <a href="https://telegram.me/IconifyOfficial"><img src="https://img.shields.io/badge/Telegram-5K+-2CA5E0?style=for-the-badge&logo=telegram&logoColor=white" alt="Telegram"></a>
  <a href="https://github.com/Mahmud0808/Iconify/releases"><img src="https://img.shields.io/github/downloads/Mahmud0808/Iconify/total?color=%233DDC84&logo=android&logoColor=%23fff&style=for-the-badge" alt="Downloads"></a>
<br><br>
<img src="https://github.com/Mahmud0808/Iconify/raw/stable/banner.png" width="100%" alt="Banner">
</p>

Iconify is an open-source android mobile application, aimed at providing users with the ability to customize various aspects of their device's user interface.

Iconify was mainly created as a substratum theme to change the system icons of any aosp rom. After some time, I converted it to use as magisk module with Terminal GUI integration. But then I got bored of using substratum and terminal gui. Applying overlays without any previews made me frustrated. So I decided to make it easier for me by creating an application where I can see the previews and change anything I want. This was totally for my personal use but as people showed interest, I decided to release it in public.

### 🌟STAR THIS REPOSITORY TO SUPPORT THE DEVELOPER AND ENCOURAGE THE DEVELOPMENT OF THE APPLICATION!
<p align="center">
<a href="https://apt.izzysoft.de/fdroid/index/apk/com.drdisagree.iconify"><img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png" width="30%" /></a>
</p>

## Features

What you can change using Iconify:

- Solid or Gradient Colors

- System & Settings Icon Packs

- Custom Brightness Bar Styles

- Custom QS Tile Shapes

- Custom QS Rows & Columns

- Custom Notification Styles

- Media Player Styles (A12 / A12.1)

- Volume Panel Styles

- QS Transparency & Blur

- Custom QS & Lockscreen Clocks

- And many more...

## Preview

<p align="center">
<a href="https://i.ibb.co/sCrYk8x/Iconify-1.png" target=”_blank”><img src="https://i.ibb.co/sCrYk8x/Iconify-1.png" alt="Preview 1" border="0" style="width: 24%; margin: 32px;"></a>
<a href="https://i.ibb.co/W3SH7dG/Iconify-2.png" target=”_blank”><img src="https://i.ibb.co/W3SH7dG/Iconify-2.png" alt="Preview 2" border="0" style="width: 24%; margin: 32px;"></a>
<a href="https://i.ibb.co/gS4nNP5/Iconify-3.png" target=”_blank”><img src="https://i.ibb.co/gS4nNP5/Iconify-3.png" alt="Preview 3" border="0" style="width: 24%; margin: 32px;"></a>
</p>

## Requirements

- Android 12+ Pixel / AOSP Based Custom Rom

- [Magisk](https://github.com/topjohnwu/Magisk) (Recommended) or [KernelSU](https://github.com/tiann/KernelSU)

- [LSPosed](https://github.com/LSPosed/LSPosed) (Optional)

- Decryption Supported Custom Recovery (Just in case)

> KSU users must flash OverlayFS Module from [Here](https://github.com/HuskyDG/magic_overlayfs/releases)
> 
> Remember to edit _mode.sh_ file inside the module and change these values:
> 
> `OVERLAY_MODE=1`
> `DO_UNMOUNT_KSU=true`

## Instructions

- Install and open the app

- Grant root permission

- Click on `CONTINUE` button

- Wait for it to finish loading

- Reboot the device

- Now just enable whatever you want from the app

## In Case of Bootloop

- Boot into safe mode ([Here's how to](https://www.androidauthority.com/how-to-enter-safe-mode-android-801476/))

- Or, Remove Iconify folder from data/adb/modules directory

## Download

> Download latest version of Iconify from here.

- [Release Build](https://github.com/Mahmud0808/Iconify/releases/latest)

- [Debug Build](https://github.com/Mahmud0808/Iconify/actions)

## Telegram

> Follow to get latest news and updates.

- [@IconifyOfficial](https://t.me/IconifyOfficial)

## Discussion & Feedback

> You can chit-chat here on topics related to Iconify.

- [GitHub Discussions](https://github.com/Mahmud0808/Iconify/discussions/new)

- [Telegram Group](https://t.me/IconifyDiscussion)

## Bug Report & Feature Request

> Make sure to mention as much details as possible.

- [GitHub Issues](https://github.com/Mahmud0808/Iconify/issues/new/choose)

## Special Thanks To

- [Android Open Source Project (AOSP)](https://source.android.com) (For Android source code)

- [Substratum](https://github.com/substratum/substratum) (For overlay building tricks)

- [icons8.com](https://icons8.com) (For giving me permission to use their icons)

- [iconsax.io](http://iconsax.io) (For in-app icon set)

- [J@i](https://t.me/jai_08) (For helping me with shell scripting)

- [Flodor](https://t.me/Rodolphe06) (For helping me with resources)

- [modestCat](https://t.me/ModestCat03) (For helping me with resources)

- [Sanely insane](https://t.me/sanely_insane) (For always being there and testing)

- [Jaguar](https://t.me/Jaguar0066) (For always being there and testing)

- [Jorge ARZ](https://t.me/ArzjoDev) (For helping me with resources)

- [ɦʏքɛʀ.sɦ](https://t.me/hyp3r_sh) (For helping me with resources)

- [AOSPMods](https://github.com/siavash79/AOSPMods) (For helping me with XPosed part)

- [HideNavBar](https://github.com/Magisk-Modules-Repo/HideNavBar) (For the navbar tweaks)

## Contribution

All contributions are welcome, from code to documentation to graphics to design suggestions to bug reports. Please use GitHub to its fullest; contribute Pull Requests, contribute tutorials or other content - whatever you have to offer, we can use it!

## Disclaimer

- Some roms might not be fully compatible with Iconify.

- I won't be responsible if anything happens to your device.

- Make sure you have custom recovery installed to revert changes if anything goes wrong.

- PremadeOverlays are used as embedded APKs to reduce the overlay compilation time. For details, [check here](https://github.com/Mahmud0808/Iconify/blob/beta/app/src/main/assets/PremadeOverlays/cheatsheet).
