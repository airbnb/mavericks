@file:Suppress("Detekt.MaxLineLength")

package com.airbnb.mvrx.dogs.data

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class DogRepository {
    fun getDogs() = Observable.fromCallable<List<Dog>> {
        Thread.sleep(2000)
        listOf(
            Dog(
                44365525,
                "Turbo",
                "Catahoula Leopard Dog Mix",
                "https://dl5zpyw5k3jeb.cloudfront.net/photos/pets/44365525/3/?bust=1554156953&width=1080",
                "Meet Turbo: Tank and Turbo are two gorgeous Catahoula mix boys who love nothing more than running and playing. These guys are approximately 9 weeks old and currently weigh around 13 pounds each. They are a good medium-large breed dog, and should grow to be around 60-70 pounds. Catahoulas have beautiful markings and these two are no different. The breed is known for being independent working dogs. Catahoulas require firm guidance and early socialization, as they can be independent, territorial, and protective. Once they know their place in the family unit, they are affectionate, loyal, and gentle."
            ),
            Dog(
                40037002,
                "Jules",
                "Terrier, Pit Bull Mix",
                "https://cdn3-www.dogtime.com/assets/uploads/gallery/pit-bull-dog-breed-pictures/pit-bull-dog-breed-picture-1.jpg",
                "His name is Jules and he is named after his favorite author Jules Verne. Jules is a happy boy who love nothing more than to hang out indoors with you, and while he may not always want to take a 'Journey to the Center of the Earth', he'll be just as happy if you read it to him or watch the film together! He enjoys good company and sun bathing, weather permitting. You don't have to travel \"20,000 leagues Under The Sea\" to meet him, you can find him at the SPCA!"
            ),
            Dog(
                39936816,
                "Pearl",
                "Shepherd Mix",
                "https://www.sfspca.org/sites/default/files/styles/480_width/public/images/animals/39936816-120fca18.jpg",
                "There's no shell around this Pearl! She's all the way out. Pearl is a very active and enthusiastic pup. While technically an adolescent pup, she's still got lots to learn. She adores being with people, plays fetch like a champ and can't wait to meet new folks and new dogs too. Pearl will thrive in a home with folks who have a more flexible schedule as she's very social and doesn't particularly like being alone for long periods of time. Lots of exercise and mental enrichment will help her grow into her very best self! Are you looking for a darling, active, friendly, snuggly, active, social and active companion? Come meet Pearl to see what all that looks like in one dog!"
            ),
            Dog(
                40379134,
                "Maya",
                "German Shepherd Mix",
                "https://www.sfspca.org/sites/default/files/styles/480_width/public/images/animals/40379134-a547eac3.jpg",
                "She is named after an ancient civilization, but there is nothing old and stuffy about this active girl. Meet Maya, a bouncy girl who is loaded with love and energy. Maya loves being active and she will always be ready to take you up on an offer to go for a walk. She will hope you bring a ball along with you since she is quite fond of chasing them. She is super affectionate and she loves getting treats. Maya needs to live in an active household where she can get lots of exercise. Come by and toss some balls around with Maya today."
            ),
            Dog(
                40706214,
                "Victor",
                "Australian Cattle Dog",
                "https://www.sfspca.org/sites/default/files/styles/480_width/public/images/animals/40706214-5b51f8de.jpg",
                "I'm a curious pup looking for adopters who will commit to continuing my puppy education and socialization. Ready for a home with snuggles and playtime and lots of puppy-love, I would love to attend puppy socials and meet other young dogs to grow in skills and confidence. Ask a staff member or check our website for details/dates of our Puppy Parent Orientation (free) class to learn how you can be my best family ever and receive 50% off my adoption fee! (PPO must be attended prior to adoption.)"
            ),
            Dog(
                39822990,
                "Wonder",
                "Shepherd Mix",
                "https://www.sfspca.org/sites/default/files/styles/480_width/public/images/animals/39822990-628b4657.jpg",
                "Have you ever wondered if there's a dog out there that's perfect for you? Wonder no more because it's probably our handsome Wonder dog! Wonder is his name and pondering the universal questions is his game. Ha! Wonder is a young, active, strong fellow, who prefers to take introductions slow and cautiously. He is hoping to find a forever home in a quieter, low dog-trafficked, neighborhood with experienced adopters. Wonder loves his own people very much and isn't too keen on sharing them with others, so he is hoping to find a special someone or two someones with whom to wander through the days ahead. Why not wander down to meet Wonder today!"
            ),
            Dog(
                44373538,
                "Fuzzy",
                "Terrier Mix",
                "https://dl5zpyw5k3jeb.cloudfront.net/photos/pets/44373538/1/?bust=1554262696&width=1080",
                "Fuzzy is a male Terrier mix and he's about a whole 8 weeks hold, which means plenty of mischief and fun trying to explore the world around him. And that means he should be finding his new forever home really soon so that he'll have fun getting to know people, places, toys, and maybe even other fur kids like him.\n" +
                    "\nDon't wait, these puppies don't last long..."
            ),
            Dog(
                44371030,
                "Happy",
                "Golden Retriever",
                "https://thehappypuppysite.com/wp-content/uploads/2017/12/puppy2.jpg",
                "What's not to love about a Golden Retriever!"
            )
        )
    }.subscribeOn(Schedulers.io())

    fun adoptDog(dog: Dog) = Single.just(dog)
        .delaySubscription(2, TimeUnit.SECONDS)
        .subscribeOn(Schedulers.io())
}
