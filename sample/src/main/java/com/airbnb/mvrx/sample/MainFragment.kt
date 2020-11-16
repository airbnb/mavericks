package com.airbnb.mvrx.sample

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.launcher.MavericksLauncherActivity
import com.airbnb.mvrx.sample.databinding.MainFragmentBinding
import com.airbnb.mvrx.sample.utils.viewBinding
import com.airbnb.mvrx.sample.views.basicRow
import com.airbnb.mvrx.sample.views.marquee

class MainFragment : Fragment(R.layout.main_fragment) {
    private val binding: MainFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerView.withModels {
            marquee {
                id("marquee")
                title("Welcome to MvRx")
                subtitle("Select a demo below")
            }

            basicRow {
                id("hello_world")
                title("Hello World")
                subtitle(demonstrates("Simple MvRx usage"))
                clickListener { _ -> findNavController().navigate(R.id.action_main_to_helloWorldFragment) }
            }

            basicRow {
                id("parent_fragments")
                title("Parent/Child ViewModel")
                subtitle(demonstrates("parentFragmentViewModel"))
                clickListener { _ -> findNavController().navigate(R.id.action_main_to_parentFragment) }
            }

            basicRow {
                id("random_dad_joke")
                title("Random Dad Joke")
                subtitle(demonstrates("fragmentViewModel", "Network requests", "Dependency Injection"))
                clickListener { _ -> findNavController().navigate(R.id.action_main_to_randomDadJokeFragment) }
            }

            basicRow {
                id("launcher")
                title("Launcher")
                subtitle(demonstrates("MvRx Launcher"))
                clickListener { _ ->
                    startActivity(Intent(requireActivity(), MavericksLauncherActivity::class.java))
                }
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
                clickListener { _ -> findNavController().navigate(R.id.action_mainFragment_to_dadJokeIndex) }
            }

            basicRow {
                id("user_flow")
                title("User Flow")
                subtitle(
                    demonstrates(
                        "Sharing data across screens",
                        "activityViewModel and existingViewModel"
                    )
                )
                clickListener { _ -> findNavController().navigate(R.id.action_main_to_flowIntroFragment) }
            }
        }
    }

    private fun demonstrates(vararg items: String) =
        arrayOf("Demonstrates:", *items).joinToString("\n\t\t• ")
}
