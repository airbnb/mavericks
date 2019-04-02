package com.airbnb.mvrx.dogs

import io.reactivex.Observable

class DogRepository {
    fun getDogs() = Observable.fromCallable<List<Dog>> {
        Thread.sleep(2000)
        listOf(
            Dog(
                40037002,
                "Jules",
                55,
                "Terrier, Pit Bull Mix",
                "Social Butterfly",
                "https://www.sfspca.org/sites/default/files/styles/480_width/public/images/animals/40037002-689b703b.jpg?itok=sZT_bhD-",
                "His name is Jules and he is named after his favorite author Jules Verne. Jules is a happy boy who love nothing more than to hang out indoors with you, and while he may not always want to take a 'Journey to the Center of the Earth', he'll be just as happy if you read it to him or watch the film together! He enjoys good company and sun bathing, weather permitting. You don't have to travel \"20,000 leagues Under The Sea\" to meet him, you can find him at the SPCA!"
            ),
            Dog(
                39936816,
                "Pearl",
                39,
                "Shepherd Mix",
                "The Great Explorer",
                "https://www.sfspca.org/sites/default/files/styles/480_width/public/images/animals/39936816-120fca18.jpg?itok=Tc9ZQB6G",
                "There's no shell around this Pearl! She's all the way out. Pearl is a very active and enthusiastic pup. While technically an adolescent pup, she's still got lots to learn. She adores being with people, plays fetch like a champ and can't wait to meet new folks and new dogs too. Pearl will thrive in a home with folks who have a more flexible schedule as she's very social and doesn't particularly like being alone for long periods of time. Lots of exercise and mental enrichment will help her grow into her very best self! Are you looking for a darling, active, friendly, snuggly, active, social and active companion? Come meet Pearl to see what all that looks like in one dog!"
            )
        )
    }
}