# Working with immutable maps
Use [custom copy and delete extension functions](https://gist.github.com/gpeal/3cf0907fc80094a833f9baa309a1f627) and treat them similar to data classes:

`setState { copy(yourMap = yourMap.copy(“a” to 1, “b” to 2)) }`

or

`setState { copy(yourMap = yourMap.delete(“a”, “b”)) }`
