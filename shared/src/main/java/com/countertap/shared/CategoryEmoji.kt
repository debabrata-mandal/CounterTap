package com.countertap.shared

fun categoryEmoji(name: String): String {
    val n = name.lowercase()
    return when {
        n.hasAny("tea", "chai")                               -> "☕"
        n.hasAny("coffee", "latte", "espresso", "cappuccino") -> "☕"
        n.hasAny("juice", "smoothie", "lassi")                -> "🥤"
        n.hasAny("shake", "milkshake")                        -> "🧋"
        n.hasAny("beverage", "drink", "soda", "cold drink")   -> "🧃"
        n.hasAny("water", "mineral")                          -> "💧"
        n.hasAny("snack", "appetizer", "starter", "finger")   -> "🍟"
        n.hasAny("biryani")                                   -> "🍚"
        n.hasAny("rice")                                      -> "🍚"
        n.hasAny("curry", "gravy", "masala")                  -> "🥘"
        n.hasAny("dal", "daal", "lentil", "soup")             -> "🥣"
        n.hasAny("main", "meal", "thali")                     -> "🍛"
        n.hasAny("roti", "naan", "paratha", "chapati", "bread") -> "🫓"
        n.hasAny("dessert", "sweet", "mithai", "halwa", "kheer") -> "🍮"
        n.hasAny("cake", "pastry", "brownie", "cookie", "muffin") -> "🎂"
        n.hasAny("ice cream", "icecream", "kulfi", "gelato")  -> "🍨"
        n.hasAny("chicken", "poultry", "wings")               -> "🍗"
        n.hasAny("mutton", "lamb", "kebab", "seekh")          -> "🍖"
        n.hasAny("fish", "seafood", "prawn", "shrimp", "crab") -> "🐟"
        n.hasAny("egg", "omelette", "omelette")               -> "🍳"
        n.hasAny("veg", "salad", "vegetable", "paneer")       -> "🥗"
        n.hasAny("pizza")                                     -> "🍕"
        n.hasAny("burger", "sandwich", "wrap", "roll")        -> "🍔"
        n.hasAny("pasta", "noodle", "chow", "hakka")          -> "🍜"
        n.hasAny("breakfast", "morning", "brunch")            -> "🍳"
        n.hasAny("combo", "set meal", "meal deal", "thali")   -> "🍱"
        n.hasAny("fruit", "fruits")                           -> "🍎"
        n.hasAny("special", "chef", "signature", "house")     -> "⭐"
        n.hasAny("pav", "vada", "dosa", "idli", "samosa")    -> "🫔"
        n.hasAny("momos", "dumpling", "dim sum")              -> "🥟"
        else                                                  -> "🍽️"
    }
}

private fun String.hasAny(vararg keywords: String) = keywords.any { contains(it) }
