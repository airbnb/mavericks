package com.airbnb.mvrx.sample

import com.airbnb.mvrx.sample.core.BaseFragment
import com.airbnb.mvrx.sample.core.simpleController
import com.airbnb.mvrx.sample.views.basicRow
import com.airbnb.mvrx.sample.views.marquee

class MainFragment : BaseFragment() {

    override fun epoxyController() = simpleController {
        marquee {
            id("marquee")
            title("Welcome to MvRx")
            subtitle("Select a demo below")
        }

        basicRow {
            id("hello_world")
            title("Hello World")
            subtitle(demonstrates("Simple MvRx usage"))
            clickListener { _ -> navigateTo(R.id.action_main_to_helloWorldFragment) }
        }

        basicRow {
            id("hello_world_epoxy")
            title("Hello World (Epoxy)")
            subtitle(demonstrates("Simple MvRx usage", "Epoxy integration"))
            clickListener { _ -> navigateTo(R.id.action_main_to_helloWorldEpoxyFragment) }
        }

        basicRow {
            id("parent_fragments")
            title("Parent/Child ViewModel")
            subtitle(demonstrates("parentFragmentViewModel"))
            clickListener { _ -> navigateTo(R.id.action_main_to_parentFragment) }
        }

        basicRow {
            id("random_dad_joke")
            title("Random Dad Joke")
            subtitle(demonstrates("fragmentViewModel", "Network requests", "Dependency Injection"))
            clickListener { _ -> navigateTo(R.id.action_main_to_randomDadJokeFragment) }
        }

        basicRow {
            id("dad_jokes")
            title("Dad Jokes")
            subtitle(
                demonstrates(
                    "fragmentViewModel",
                    "Fragment arguments",
                    "Network requests",
                    "Pagination",
                    "Dependency Injection"
                )
            )
            clickListener { _ -> navigateTo(R.id.action_mainFragment_to_dadJokeIndex) }
        }

        basicRow {
            id("flow")
            title("Flow")
            subtitle(
                demonstrates(
                    "Sharing data across screens",
                    "activityViewModel and existingViewModel"
                )
            )
            clickListener { _ -> navigateTo(R.id.action_main_to_flowIntroFragment) }
        }
    }

    private fun demonstrates(vararg items: String) =
        arrayOf("Demonstrates:", *items).joinToString("\n\t\t• ")
}