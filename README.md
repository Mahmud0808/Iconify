<div align="center">
  <img src="https://raw.githubusercontent.com/Mahmud0808/Iconify/beta/.github/resources/banner.png" width="100%" alt="Banner">
  
  # v6.5.1 is out!
</div>
<p align="center">
  <a href="https://github.com/Mahmud0808/Iconify/releases"><img src="https://img.shields.io/github/downloads/Mahmud0808/Iconify/total?color=%233DDC84&logo=android&logoColor=%23fff&style=for-the-badge" alt="Downloads"></a>
  <a href="https://github.com/Mahmud0808/Iconify"><img alt="Repo Size" src="https://img.shields.io/github/repo-size/Mahmud0808/Iconify?style=for-the-badge"></a>
  <a href="https://github.com/Mahmud0808/Iconify/actions"><img src="https://img.shields.io/github/actions/workflow/status/Mahmud0808/Iconify/build_debug.yml?branch=beta&label=Debug%20Build&style=for-the-badge" alt="Debug Build"></a>
  <a href="https://telegram.me/IconifyOfficial"><img src="https://img.shields.io/badge/Telegram-5K+-2CA5E0?style=for-the-badge&logo=telegram&logoColor=white" alt="Telegram"></a>
</p>
<div align="center">
  <a href="https://apt.izzysoft.de/fdroid/index/apk/com.drdisagree.iconify"><img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png" width="30%"></a>

# Iconify

### Free and Open-Source Android Customizer Application
</div>
<p align="center">
Iconify is an open-source Android mobile application aimed at providing users with the ability to customize various aspects of their device's user interface. 📱
<br><br>
Furthermore, the open-source nature of Iconify encourages community contributions and continuous improvement, ensuring a dynamic and evolving user experience. 🤝
</p>
<div align="center">
  <br>
  <a href="https://www.buymeacoffee.com/DrDisagree"><img src="https://github.com/Mahmud0808/Iconify/blob/beta/.github/resources/bmc-button.png" width="30%" alt="Buy me a coffee" /></a>
  <br><br>
  <img src="https://raw.githubusercontent.com/Mahmud0808/Iconify/beta/.github/resources/features.png" width="100%" alt="Features">
</div>

```diff
- NOTE: This app requires Magisk or KernelSU for root access. Any alternative methods won't work.
```

# 🛠 Requirements

- Android 12+ Pixel / AOSP based custom ROM

- [Magisk](https://github.com/topjohnwu/Magisk) (Recommended) or [KernelSU](https://github.com/tiann/KernelSU)

- [LSPosed](https://github.com/LSPosed/LSPosed) (Optional)

- Decryption Supported Custom Recovery (Just in case)

> KSU users must flash OverlayFS Module from [**HERE**](https://github.com/HuskyDG/magic_overlayfs/releases) before installing iconify.
> 
> Remember to edit _mode.sh_ file inside the module and change these values before flashing:
> 
> `OVERLAY_MODE=1`
> `DO_UNMOUNT_KSU=true`

# 👨‍💻 Installation 

1. Go to [Releases](https://github.com/Mahmud0808/Iconify/releases) section.

2. Download the `.apk` file.

3. Install and open the application.

4. Follow the instructions, wait for finishing overlay building process.

5. Reboot the device when prompted, profit.

# 🤫 Roadmap

You can track all the progress [HERE](https://github.com/Mahmud0808/Iconify/commits/beta)

- Fix bugs.

- Improve UI/UX.

# 🤝 Contribution

We highly appreciate and welcome all forms of contributions, ranging from code, documentation, graphics, design suggestions, to bug reports. We encourage you to make the most of GitHub's collaborative platform by submitting Pull Requests, providing tutorials or other relevant content. Whatever you have to offer, we value and can utilize it effectively in our project.

# 🌐 Translation

You can help translate Iconify [on Crowdin](https://crowdin.com/project/iconify). Here are a few tips:

- When using quotation marks, insert the symbols specific to the target language, as listed in [this table](https://en.wikipedia.org/wiki/Quotation_mark#Summary_table).

- Iconify uses title case for some English UI text. Title case isn’t used in other languages; opt for sentence case instead.

- Some English terminology may have no commonly used equivalents in other languages. In such cases, use short descriptive phrases–for example, the equivalent of _shade generator_ for _monet engine_.

# 🤓 FAQ

- How does Iconify work?
  - Iconify uses Android overlays that allows modifications to be applied to the user interface (UI) of the device without directly modifying the underlying system files.
- Do I need a root access for Iconify to work?
  - Yes, root access is required for Iconify to function properly. While Iconify does support KernelSU, it is highly recommended to use Magisk for the best compatibility.
- Why is LSPosed listed as an optional requirement?
  - LSPosed is categorized as optional due to the fact that even without its installation, you can access the majority of the features offered by iconify. However, should you choose to install LSPosed, you will gain access to certain additional features such as header clocks, lockscreen clocks, header image, battery styles, among others.
- Which devices does Iconify support?
  - Iconify exclusively supports stock Pixel or custom AOSP ROMs. It does not support other OEMs such as MIUI, OneUI, ColorOS, NothingOS, MotoUI, etc.
- Is Android "X" supported?
  - Iconify officially supports Android 12 and later versions. Compatibility with earlier Android versions is not provided, and there are no plans to introduce support for those versions.
- I got bootloop. How do I fix it?
  - Boot into [Safe Mode](https://www.androidauthority.com/how-to-enter-safe-mode-android-801476/). Or, you can remove the Iconify folder from /data/adb/modules/ using custom recovery.
- What is the difference between Release build and Debug build?
  - [Release build](https://github.com/Mahmud0808/Iconify/releases/latest) is an optimized version intended for distribution to end-users, while [Debug build](https://github.com/Mahmud0808/Iconify/actions) includes additional features and information for debugging and development purposes.
- Can I use Iconify in conjunction with other customization apps?
  - Yes, Iconify can be used alongside other customization apps. However, it's important to note that conflicts or overlapping modifications may occur, which could affect the overall user experience.
- Is there any official support available for Iconify?
  - Yes, you can visit the official [Iconify telegram group](https://t.me/IconifyDiscussion) to access resources, seek assistance, and engage with other Iconify users.
- I found a bug. How do I report it?
  - To report a bug, please navigate to the [Issues](https://github.com/Mahmud0808/Iconify/issues/new/choose) section. Create a new issue and ensure you select the `Bug Report` template. Provide as much detailed information as possible, including steps to reproduce the bug and any relevant error messages or screenshots.
- How do I request a new feature?
  - If you have a feature request, please go to the [Issues](https://github.com/Mahmud0808/Iconify/issues/new/choose) section. Create a new issue and choose the `Feature Request` template. Be sure to include comprehensive details about the desired feature, its potential benefits, and any other relevant information that can assist in understanding and evaluating the request.
- Where can I make a donation?
  - The preferred and designated means for donating to the project's developer is via the "[Buy me a coffee](https://www.buymeacoffee.com/DrDisagree)" page.

# ❤ Credits 

### Thanks to:

- [Android Open Source Project (AOSP)](https://source.android.com) for Android source code.
- [Substratum](https://github.com/substratum/substratum) for overlay building tricks.
- [icons8.com](https://icons8.com) for giving me permission to use their icons.
- [iconsax.io](http://iconsax.io) for in-app icon set.
- [@Jai](https://t.me/jai_08) for helping me with shell script.
- [@Flodor](https://t.me/Rodolphe06), [@modestCat](https://t.me/ModestCat03), [@Jorge ARZ](https://t.me/ArzjoDev), [@ɦʏքɛʀ.sɦ](https://t.me/hyp3r_sh) for helping with resources.
- [AOSPMods](https://github.com/siavash79/AOSPMods), [@siavash79](https://t.me/siavash7999) for helping me with Xposed mods.
- [@Sanely_insane](https://t.me/sanely_insane), [@Jaguar](https://t.me/Jaguar0066) for support and motivation.
- [HideNavBar](https://github.com/Magisk-Modules-Repo/HideNavBar) for the navbar tweaks.
- And everyone who contributed... :)

### Translators:

- Arabic [@MRX7014](https://github.com/mrx7014), [@Mohamed Bahaa](https://github.com/muhammadbahaa2001)
- French [@MXC48](https://github.com/MXC48)
- Indonesian [@KaeruShi](https://github.com/KaeruShi)
- Persian [@Faceless1999](https://github.com/Faceless1999)
- Polish [@SK00RUPA](https://github.com/SK00RUPA)
- Portuguese [@ElTifo](https://github.com/ElTifo)
- Russian [@B1ays](https://github.com/B1ays)
- Simplified Chinese [@Cccc-owo](https://github.com/Cccc-owo)
- Spanish [@luckkmaxx](https://github.com/luckkmaxx)
- Turkish [@serhat-demir](https://github.com/serhat-demir), [@Emre](https://crowdin.com/profile/khapnols), [@WINZORT](https://crowdin.com/profile/linuxthegoat)
- Vietnamese [@viettel1211](https://t.me/viettel1211)

# 📝 Disclaimer

- Please note that Iconify may not be fully compatible with all custom ROMs. It is discouraged to use on heavily modified ROMs.
- I cannot be held responsible for any potential damage or issues that may occur to your device while using Iconify.
- It is highly recommended to have a custom recovery installed on your device to revert any changes in case of unexpected problems.
- The use of PremadeOverlays involves the inclusion of embedded APKs to expedite overlay compilation. For the source code, [click here](https://github.com/Mahmud0808/Iconify/blob/beta/app/src/main/assets/PremadeOverlays/cheatsheet).
