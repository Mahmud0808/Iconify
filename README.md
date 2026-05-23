<div align="center">
  <img src="https://raw.githubusercontent.com/Mahmud0808/Iconify/beta/.github/resources/banner.png" width="100%" alt="Banner">

# v8.0.0 is out!
</div>
<p align="center">
  <a href="https://github.com/Mahmud0808/Iconify/releases"><img src="https://img.shields.io/github/downloads/Mahmud0808/Iconify/total?color=%233DDC84&logo=android&logoColor=%23fff&style=for-the-badge" alt="Downloads"></a>
  <a href="https://github.com/Mahmud0808/Iconify"><img alt="Repo Size" src="https://img.shields.io/github/repo-size/Mahmud0808/Iconify?style=for-the-badge"></a>
  <a href="https://github.com/Mahmud0808/Iconify/actions"><img src="https://img.shields.io/github/actions/workflow/status/Mahmud0808/Iconify/build_debug.yml?branch=beta&label=Debug%20Build&style=for-the-badge" alt="Debug Build"></a>
  <a href="https://t.me/DrDsProjects"><img src="https://img.shields.io/badge/Telegram-5K+-2CA5E0?style=for-the-badge&logo=telegram&logoColor=white" alt="Telegram"></a>
</p>
<div align="center">

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

> [!CAUTION]
>
> This app requires Magisk/KernelSU/APatch for root access. Any alternative methods won't work.

# 🛠 Requirements

- **Android 12+ Pixel / AOSP based custom ROM**:
    - **Android 12 to Android 16 (non-QPR)**: Use [v7.3.0](https://github.com/Mahmud0808/Iconify/releases/tag/v7.3.0)
    - **Android 16 QPR1 and later**: Use [v8.0.0](https://github.com/Mahmud0808/Iconify/releases/tag/v8.0.0) and later

- [Magisk](https://github.com/topjohnwu/Magisk) / [KernelSU](https://github.com/tiann/KernelSU) / [APatch](https://github.com/bmax121/APatch)
    - **KernelSU v3.0+** users must install [Mountify](https://github.com/backslashxx/mountify/releases/latest) (or another metamodule of choice), otherwise some features will be missing in the app.

- [LSPosed](https://github.com/LSPosed/LSPosed) / [Vector](https://github.com/JingMatrix/Vector) / Other Xposed framework

- Decryption Supported Custom Recovery (Just in case)

# 👨‍💻 Installation

### Installation for Magisk Users:

1. Download and install the Iconify app.

2. Open the app, grant root permission and follow the instructions.

3. Wait for it to finish generating rom specific module.

4. Reboot the device when prompted, profit!

### Installation for KernelSU/APatch Users:

1. **For KernelSU / KernelSU Next v3.0+ Users**:
   <details>
     <summary><b>Click to show Mountify setup instructions (Required for systemless /system modification)</b></summary>

    * Uninstall the Iconify module if present.
    * Download and install the latest [Mountify](https://github.com/backslashxx/mountify/releases/latest) module and reboot.
    * Configure Mountify:
      ```env
      mountify_mounts=2
      MOUNT_DEVICE_NAME=KSU
      mountify_custom_umount=0
      ```
    * Reboot again.

   *Ps: You can try changing the value of `mountify_custom_umount` according to your needs. You can try other metamodules if Mountify doesn't work.*
   </details>

2. Flash [ZygiskNext](https://github.com/Dr-TSNG/ZygiskNext/releases/latest) and [LSPosed](https://github.com/LSPosed/LSPosed) / [Vector](https://github.com/JingMatrix/Vector) module (only if you want to use LSPosed features).

3. Reboot your device to get the module installed successfully.

4. Now follow the installation steps of magisk users and voila!

> [!TIP]
>
> KernelSU doesn't show root prompt. You will have to manually enable root access for Iconify from the KernelSU app itself.
> In some cases, you may need to disable the `Umount modules by default` option from KernelSU settings if Iconify changes are not reflecting.

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

<details>
  <summary>How does Iconify work?</summary>

- Iconify uses Android overlays and Xposed framework that allows modifications to be applied to the user interface (UI) of the device without directly modifying the underlying system files.
</details>

<details>
  <summary>Do I need a root access for Iconify to work?</summary>

- Yes, root access is required for Iconify to function properly. While Iconify does support KernelSU and APatch, it is highly recommended to use Magisk for the best compatibility.
</details>

<details>
  <summary>Why is LSPosed listed as an optional requirement?</summary>

- LSPosed is categorized as optional due to the fact that even without its installation, you can access the majority of the features offered by iconify. However, should you choose to install LSPosed, you will gain access to certain additional features such as header clocks, lockscreen clocks, header image, battery styles, among others.
</details>

<details>
  <summary>Which devices does Iconify support?</summary>

- Iconify exclusively supports stock Pixel or custom AOSP ROMs. It does not support other OEMs such as MIUI, OneUI, ColorOS, NothingOS, MotoUI, etc.
</details>

<details>
  <summary>Is Android version "xx" supported?</summary>

- Iconify officially supports Android 12 and later versions.
    - **Android 12 to Android 16 (non-QPR)**: Use v7.3.0
    - **Android 16 QPR1 and later**: Use v8.0.0 and later
- Compatibility with earlier Android versions is not provided, and there are no plans to introduce support for those versions.
</details>

<details>
  <summary>I got bootloop. How do I fix it?</summary>

- Boot into [Safe Mode](https://www.androidauthority.com/how-to-enter-safe-mode-android-801476/). Or, you can remove the Iconify folder from /data/adb/modules/ using custom recovery.
</details>

<details>
  <summary>What is the difference between Release build and Debug build?</summary>

- [Release build](https://github.com/Mahmud0808/Iconify/releases/latest) is an optimized version intended for distribution to end-users, while [Debug build](https://github.com/Mahmud0808/Iconify/actions) includes additional features and information for debugging and development purposes.
</details>

<details>
  <summary>What is the difference between FOSS build and Standard build?</summary>

- FOSS does not include [ML Kit](https://developers.google.com/ml-kit/vision/subject-segmentation/android), which is essential for automatically extracting subjects from depth wallpapers using AI. Everything else remains the same.
</details>

<details>
  <summary>Can I use Iconify in conjunction with other customization apps?</summary>

- Yes, Iconify can be used alongside other customization apps. However, it's important to note that conflicts or overlapping modifications may occur, which could affect the overall user experience.
</details>

<details>
  <summary>Is there any official support available for Iconify?</summary>

- Yes, you can visit the official [Iconify telegram group](https://t.me/DrDsProjectsChat) to access resources, seek assistance, and engage with other Iconify users.
</details>

<details>
  <summary>I found a bug. How do I report it?</summary>

- To report a bug, please navigate to the [Issues](https://github.com/Mahmud0808/Iconify/issues/new/choose) section. Create a new issue and ensure you select the `Bug Report` template. Provide as much detailed information as possible, including steps to reproduce the bug and any relevant error messages or screenshots.
</details>

<details>
  <summary>How do I request a new feature?</summary>

- If you have a feature request, please go to the [Issues](https://github.com/Mahmud0808/Iconify/issues/new/choose) section. Create a new issue and choose the `Feature Request` template. Be sure to include comprehensive details about the desired feature, its potential benefits, and any other relevant information that can assist in understanding and evaluating the request.
</details>

<details>
  <summary>Where can I make a donation?</summary>

- The preferred and designated means for donating to the project's developer is via the "[Buy me a coffee](https://www.buymeacoffee.com/DrDisagree)" page.
</details>

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
- And everyone who [contributed](https://github.com/Mahmud0808/Iconify/blob/beta/docs/contributors.md) and [translated](https://github.com/Mahmud0808/Iconify/blob/beta/docs/translators.md)... :)

# 📝 Disclaimer

> [!WARNING]
> - Please note that Iconify may not be fully compatible with all custom ROMs. It is discouraged to use on heavily modified ROMs.
> - I cannot be held responsible for any potential damage or issues that may occur to your device while using Iconify.
> - It is highly recommended to have a custom recovery installed on your device to revert any changes in case of unexpected problems.
