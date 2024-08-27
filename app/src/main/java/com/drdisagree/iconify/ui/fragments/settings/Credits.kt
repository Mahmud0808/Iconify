package com.drdisagree.iconify.ui.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.databinding.FragmentCreditsBinding
import com.drdisagree.iconify.ui.adapters.InfoAdapter
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.models.InfoModel
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader

class Credits : BaseFragment() {

    private lateinit var binding: FragmentCreditsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreditsBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.section_title_credits
        )

        // RecyclerView
        binding.infoContainer.setLayoutManager(LinearLayoutManager(requireContext()))

        val adapter = ConcatAdapter(
            initCreditsList(),
            initContributorsList(),
            initTranslatorsList()
        )

        binding.infoContainer.setAdapter(adapter)
        binding.infoContainer.setHasFixedSize(true)

        return view
    }

    private fun initCreditsList(): InfoAdapter {
        val creditsList = ArrayList<InfoModel>().apply {
            add(InfoModel(resources.getString(R.string.section_title_thanks)))

            add(
                InfoModel(
                    requireContext(),
                    "Icons8.com",
                    appContextLocale.resources.getString(R.string.info_icons8_desc),
                    "https://icons8.com/",
                    R.drawable.ic_link
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "iconsax.io",
                    appContextLocale.resources.getString(R.string.info_iconsax_desc),
                    "http://iconsax.io/",
                    R.drawable.ic_link
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "Siavash",
                    appContextLocale.resources.getString(R.string.info_xposed_desc),
                    "https://t.me/siavash7999",
                    R.drawable.ic_user
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "Jai",
                    appContextLocale.resources.getString(R.string.info_shell_desc),
                    "https://t.me/Jai_08",
                    R.drawable.ic_user
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "1perialf",
                    appContextLocale.resources.getString(R.string.info_rro_desc),
                    "https://t.me/Rodolphe06",
                    R.drawable.ic_user
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "modestCat",
                    appContextLocale.resources.getString(R.string.info_rro_desc),
                    "https://t.me/ModestCat03",
                    R.drawable.ic_user
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "Sanely Insane",
                    appContextLocale.resources.getString(R.string.info_tester_desc),
                    "https://t.me/sanely_insane",
                    R.drawable.ic_user
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "Jaguar",
                    appContextLocale.resources.getString(R.string.info_tester_desc),
                    "https://t.me/Jaguar0066",
                    R.drawable.ic_user
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "hani & TeamFiles",
                    appContextLocale.resources.getString(R.string.info_betterqs_desc),
                    "https://github.com/itsHanibee",
                    R.drawable.ic_user
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "AAGaming",
                    appContextLocale.resources.getString(R.string.info_binaries_desc),
                    "https://aagaming.me",
                    R.drawable.ic_user
                )
            )
        }

        return InfoAdapter(
            requireContext(),
            creditsList
        )
    }

    private fun initContributorsList(): InfoAdapter {
        val contributorsList = ArrayList<InfoModel>().apply {
            add(InfoModel(resources.getString(R.string.section_title_contributors)))

            add(
                InfoModel(
                    requireContext(),
                    "Luigi",
                    appContextLocale.resources.getString(R.string.info_contributor_desc),
                    "https://github.com/DHD2280",
                    R.drawable.ic_user
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "Azure-Helper",
                    appContextLocale.resources.getString(R.string.info_contributor_desc),
                    "https://github.com/Azure-Helper",
                    R.drawable.ic_user
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "HiFIi",
                    appContextLocale.resources.getString(R.string.info_contributor_desc),
                    "https://github.com/HiFIi",
                    R.drawable.ic_user
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "IzzySoft",
                    appContextLocale.resources.getString(R.string.info_contributor_desc),
                    "https://github.com/IzzySoft",
                    R.drawable.ic_user
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "Blays",
                    appContextLocale.resources.getString(R.string.info_contributor_desc),
                    "https://github.com/B1ays",
                    R.drawable.ic_user
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "Libra420T",
                    appContextLocale.resources.getString(R.string.info_contributor_desc_2),
                    "https://t.me/Libra420T",
                    R.drawable.ic_user
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "mohamedamrnady",
                    appContextLocale.resources.getString(R.string.info_contributor_desc),
                    "https://github.com/mohamedamrnady",
                    R.drawable.ic_user
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "H1mJT",
                    appContextLocale.resources.getString(R.string.info_contributor_desc),
                    "https://github.com/H1mJT",
                    R.drawable.ic_user
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "KaeruShi",
                    appContextLocale.resources.getString(R.string.info_contributor_desc),
                    "https://github.com/KaeruShi",
                    R.drawable.ic_user
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "Displax",
                    appContextLocale.resources.getString(R.string.info_contributor_desc),
                    "https://github.com/Displax",
                    R.drawable.ic_user
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "DHD2280",
                    appContextLocale.resources.getString(R.string.info_contributor_desc),
                    "https://github.com/DHD2280",
                    R.drawable.ic_user
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "armv7a",
                    appContextLocale.resources.getString(R.string.info_contributor_desc),
                    "https://github.com/armv7a",
                    R.drawable.ic_user
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "Jvr",
                    appContextLocale.resources.getString(R.string.info_contributor_desc),
                    "https://github.com/Jvr2022",
                    R.drawable.ic_user
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "ꃅꈤꀸꋪꀘ",
                    appContextLocale.resources.getString(R.string.info_contributor_desc),
                    "https://t.me/therealhndrk",
                    R.drawable.ic_user
                )
            )
        }

        return InfoAdapter(
            requireContext(),
            contributorsList
        )
    }

    private fun initTranslatorsList(): InfoAdapter {
        val translatorsList = ArrayList<InfoModel>().apply {
            add(InfoModel(resources.getString(R.string.section_title_translators)))

            add(
                InfoModel(
                    requireContext(),
                    "MRX7014",
                    appContextLocale.resources.getString(R.string.ar_translation),
                    "https://github.com/mrx7014",
                    R.drawable.flag_sa
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "Mohamed Bahaa",
                    appContextLocale.resources.getString(R.string.ar_translation),
                    "https://github.com/muhammadbahaa2001",
                    R.drawable.flag_sa
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "MXC48",
                    appContextLocale.resources.getString(R.string.fr_translation),
                    "https://github.com/MXC48",
                    R.drawable.flag_fr
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "KaeruShi",
                    appContextLocale.resources.getString(R.string.id_translation),
                    "https://github.com/KaeruShi",
                    R.drawable.flag_id
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "Danilo Belmonte",
                    appContextLocale.resources.getString(R.string.it_translation),
                    "https://crowdin.com/profile/steve.burnside",
                    R.drawable.flag_it
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "Faceless1999",
                    appContextLocale.resources.getString(R.string.fa_translation),
                    "https://github.com/Faceless1999",
                    R.drawable.flag_ir
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "igor",
                    appContextLocale.resources.getString(R.string.pt_br_translation),
                    "https://github.com/igormiguell",
                    R.drawable.flag_br
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "ElTifo",
                    appContextLocale.resources.getString(R.string.pt_translation),
                    "https://github.com/ElTifo",
                    R.drawable.flag_pt
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "Blays",
                    appContextLocale.resources.getString(R.string.ru_translation),
                    "https://github.com/B1ays",
                    R.drawable.flag_ru
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "Cccc_",
                    appContextLocale.resources.getString(R.string.zh_cn_translation),
                    "https://github.com/Cccc-owo",
                    R.drawable.flag_cn
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "Zhang chunyu",
                    appContextLocale.resources.getString(R.string.zh_tw_translation),
                    "https://crowdin.com/profile/gyah4",
                    R.drawable.flag_cn
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "luckkmaxx",
                    appContextLocale.resources.getString(R.string.es_translation),
                    "https://github.com/luckkmaxx",
                    R.drawable.flag_es
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "Serhat Demir",
                    appContextLocale.resources.getString(R.string.tr_translation),
                    "https://github.com/serhat-demir",
                    R.drawable.flag_tr
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "Emre",
                    appContextLocale.resources.getString(R.string.tr_translation),
                    "https://crowdin.com/profile/khapnols",
                    R.drawable.flag_tr
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "W͏ I͏ N͏ Z͏ O͏ R͏ T͏",
                    appContextLocale.resources.getString(R.string.tr_translation),
                    "https://github.com/mikropsoft",
                    R.drawable.flag_tr
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "Đức Trọng",
                    appContextLocale.resources.getString(R.string.vi_translation),
                    "https://t.me/viettel1211",
                    R.drawable.flag_vn
                )
            )
            add(
                InfoModel(
                    requireContext(),
                    "SK00RUPA",
                    appContextLocale.resources.getString(R.string.pl_translation),
                    "https://github.com/SK00RUPA",
                    R.drawable.flag_pl
                )
            )
        }

        return InfoAdapter(
            requireContext(),
            translatorsList
        )
    }
}
