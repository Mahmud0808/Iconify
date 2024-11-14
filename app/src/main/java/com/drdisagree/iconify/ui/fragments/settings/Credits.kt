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
import com.drdisagree.iconify.utils.parseContributors
import com.drdisagree.iconify.utils.parseTranslators

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
                    "Icons8.com",
                    appContextLocale.resources.getString(R.string.info_icons8_desc),
                    "https://icons8.com/",
                    R.drawable.ic_link
                )
            )
            add(
                InfoModel(
                    "iconsax.io",
                    appContextLocale.resources.getString(R.string.info_iconsax_desc),
                    "http://iconsax.io/",
                    R.drawable.ic_link
                )
            )
            add(
                InfoModel(
                    "Siavash",
                    appContextLocale.resources.getString(R.string.info_xposed_desc),
                    "https://t.me/siavash7999",
                    R.drawable.ic_user
                )
            )
            add(
                InfoModel(
                    "Jai",
                    appContextLocale.resources.getString(R.string.info_shell_desc),
                    "https://t.me/Jai_08",
                    R.drawable.ic_user
                )
            )
            add(
                InfoModel(
                    "1perialf",
                    appContextLocale.resources.getString(R.string.info_rro_desc),
                    "https://t.me/Rodolphe06",
                    R.drawable.ic_user
                )
            )
            add(
                InfoModel(
                    "modestCat",
                    appContextLocale.resources.getString(R.string.info_rro_desc),
                    "https://t.me/ModestCat03",
                    R.drawable.ic_user
                )
            )
            add(
                InfoModel(
                    "Sanely Insane",
                    appContextLocale.resources.getString(R.string.info_tester_desc),
                    "https://t.me/sanely_insane",
                    R.drawable.ic_user
                )
            )
            add(
                InfoModel(
                    "Jaguar",
                    appContextLocale.resources.getString(R.string.info_tester_desc),
                    "https://t.me/Jaguar0066",
                    R.drawable.ic_user
                )
            )
            add(
                InfoModel(
                    "hani & TeamFiles",
                    appContextLocale.resources.getString(R.string.info_betterqs_desc),
                    "https://github.com/itsHanibee",
                    R.drawable.ic_user
                )
            )
            add(
                InfoModel(
                    "AAGaming",
                    appContextLocale.resources.getString(R.string.info_binaries_desc),
                    "https://aagaming.me",
                    R.drawable.ic_user
                )
            )
            add(
                InfoModel(
                    "Buttercup Theme",
                    appContextLocale.resources.getString(R.string.info_buttercup_desc),
                    "https://t.me/buttercup_theme",
                    R.drawable.ic_link
                )
            )
        }

        return InfoAdapter(
            requireContext(),
            creditsList
        )
    }

    private fun initContributorsList(): InfoAdapter {
        return InfoAdapter(
            requireContext(),
            parseContributors().also {
                it.add(0, InfoModel(resources.getString(R.string.section_title_contributors)))
            }
        )
    }

    private fun initTranslatorsList(): InfoAdapter {
        return InfoAdapter(
            requireContext(),
            parseTranslators().also {
                it.add(0, InfoModel(resources.getString(R.string.section_title_translators)))
            }
        )
    }
}
