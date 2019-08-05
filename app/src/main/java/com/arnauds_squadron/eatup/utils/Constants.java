package com.arnauds_squadron.eatup.utils;

import com.parse.ParseUser;

public final class Constants {
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME =
            "com.google.android.gms.location.sample.locationaddress";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME +
            ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
            ".LOCATION_DATA_EXTRA";
    public static final String KEY_PROFILE_PICTURE = "profilePicture";
    public static final String BIO = "bio";
    public static final String DISPLAY_NAME = "displayName";
    public static final String AVG_RATING_HOST = "avgRatingsHost";
    public static final String NUM_RATINGS_HOST = "numRatingsHost";
    public static final String AVG_RATINGS_GUEST = "avgRatingsGuest";
    public static final String NUM_RATINGS_GUEST = "numRatingsGuest";
    public static final String GUEST = "guest";
    public static final String HOST = "host";
    public static final float NO_RATING = 0f;
    // Maximum number of guests
    public static final int MAX_GUESTS = 100;

    // search constants
    public static final String SEARCH_CATEGORY = "searchCategory";
    public static final int NO_SEARCH = 0;
    public static final int CUISINE_SEARCH = 1;
    public static final int LOCATION_SEARCH = 2;

    // Update speed to refresh messages: 1 second
    public static final int CHAT_UPDATE_SPEED_MILLIS = 2000;
    // Update speed to refresh events: 5 seconds
    public static final int EVENT_UPDATE_SPEED_MILLIS = 5000;

    // PagerAdapter constants
    public static final int MAIN_PAGER_START_PAGE = 2;
    public static final int ADDRESS_FRAGMENT_INDEX = 2;
    public static final int DATE_FRAGMENT_INDEX = 3;

    // The logged in user, only changed in the login activity
    public static ParseUser CURRENT_USER;

    public static final String PREFS_NAME = "preferences";
    public static final String FIRST_LOAD = "first_load";
    public static final int LAST_WALKTHROUGH_SCREEN = 3;

    // Channel id for push notifications
    public final static String CHANNEL_ID = "Toast";

    // hardcoded category suggestions for search
    public static final String[] CATEGORY_TITLE = {"Acai Bowls",
            "Afghan",
            "African",
            "Andalusian",
            "Arabian",
            "Argentine",
            "Armenian",
            "Asian Fusion",
            "Asturian",
            "Australian",
            "Austrian",
            "Backshop",
            "Bagels",
            "Baguettes",
            "Bakeries",
            "Bangladeshi",
            "Basque",
            "Bavarian",
            "Barbeque",
            "Beer, Wine & Spirits",
            "Beer Garden",
            "Beer Hall",
            "Beisl",
            "Belgian",
            "Bento",
            "Beverage Store",
            "Bistros",
            "Black Sea",
            "Brasseries",
            "Brazilian",
            "Breakfast & Brunch",
            "Breweries",
            "British",
            "Bubble Tea",
            "Buffets",
            "Bulgarian",
            "Burgers",
            "Burmese",
            "Butcher",
            "Cafes",
            "Cafeteria",
            "Cajun/Creole",
            "Patisserie/Cake Shop",
            "Cambodian",
            "Canteen",
            "Caribbean",
            "Catalan",
            "Cheesesteaks",
            "Chicken Wings",
            "Chicken Shop",
            "Chilean",
            "Chimney Cakes",
            "Chinese",
            "Churros",
            "Cideries",
            "Coffee & Tea",
            "Coffee Roasteries",
            "Coffee & Tea Supplies",
            "Comfort Food",
            "Convenience Stores",
            "Corsican",
            "Creperies",
            "CSA",
            "Cuban",
            "Cupcakes",
            "Curry Sausage",
            "Custom Cakes",
            "Cypriot",
            "Czech",
            "Czech/Slovakian",
            "Danish",
            "Delicatessen",
            "Delis",
            "Desserts",
            "Diners",
            "Dinner Theater",
            "Distilleries",
            "Do-It-Yourself Food",
            "Donairs",
            "Donuts",
            "Dumplings",
            "Eastern European",
            "Parent Cafes",
            "Empanadas",
            "Eritrean",
            "Ethical Grocery",
            "Ethiopian",
            "Farmers Market",
            "Filipino",
            "Fischbroetchen",
            "Fishmonger",
            "Fish & Chips",
            "Flatbread",
            "Fondue",
            "Food Court",
            "Food Delivery Services",
            "Food Stands",
            "Food Trucks",
            "Freiduria",
            "French",
            "Friterie",
            "Galician",
            "Game Meat",
            "Gastropubs",
            "Gelato",
            "Georgian",
            "German",
            "Giblets",
            "Mulled Wine",
            "Gluten-Free",
            "Specialty Food",
            "Greek",
            "Grocery",
            "Guamanian",
            "Halal",
            "Hawaiian",
            "Hawker Centre",
            "Heuriger",
            "Himalayan/Nepalese",
            "Hong Kong Style Cafe",
            "Honduran",
            "Honey",
            "Hot Dogs",
            "Fast Food",
            "Hot Pot",
            "Hungarian",
            "Iberian",
            "Ice Cream & Frozen Yogurt",
            "Imported Food",
            "Indonesian",
            "Indian",
            "International",
            "Internet Cafes",
            "International Grocery",
            "Irish",
            "Island Pub",
            "Israeli",
            "Italian",
            "Japanese",
            "Jewish",
            "Japanese Sweets",
            "Juice Bars & Smoothies",
            "Kebab",
            "Kiosk",
            "Kombucha",
            "Kopitiam",
            "Korean",
            "Kosher",
            "Kurdish",
            "Laos",
            "Laotian",
            "Latin American",
            "Lyonnais",
            "Malaysian",
            "Meaderies",
            "Meatballs",
            "Mediterranean",
            "Mexican",
            "Middle Eastern",
            "Milk Bars",
            "Milkshake Bars",
            "Modern Australian",
            "Modern European",
            "Mongolian",
            "Moroccan",
            "Nasi Lemak",
            "American (New)",
            "Canadian (New)",
            "New Mexican Cuisine",
            "New Zealand",
            "Nicaraguan",
            "Night Food",
            "Nikkei",
            "Noodles",
            "Norcinerie",
            "Traditional Norwegian",
            "Open Sandwiches",
            "Organic Stores",
            "Oriental",
            "Pakistani",
            "Pan Asian",
            "Panzerotti",
            "Parma",
            "Persian/Iranian",
            "Peruvian",
            "PF/Comercial",
            "Piadina",
            "Pita",
            "Pizza",
            "Poke",
            "Polish",
            "Polynesian",
            "Pop-Up Restaurants",
            "Portuguese",
            "Potatoes",
            "Poutineries",
            "Pretzels",
            "Pub Food",
            "Live/Raw Food",
            "Rice",
            "Romanian",
            "Rotisserie Chicken",
            "Russian",
            "Salad",
            "Salumerie",
            "Sandwiches",
            "Scandinavian",
            "Schnitzel",
            "Scottish",
            "Seafood",
            "Serbo Croatian",
            "Shaved Ice",
            "Shaved Snow",
            "Signature Cuisine",
            "Singaporean",
            "Slovakian",
            "Smokehouse",
            "Somali",
            "Soul Food",
            "Soup",
            "Southern",
            "Spanish",
            "Sri Lankan",
            "Steakhouses",
            "Street Vendors",
            "French Southwest",
            "Sugar Shacks",
            "Supper Clubs",
            "Sushi Bars",
            "Swabian",
            "Swedish",
            "Swiss Food",
            "Syrian",
            "Tabernas",
            "Taiwanese",
            "Tapas Bars",
            "Tapas/Small Plates",
            "Tavola Calda",
            "Tea Rooms",
            "Tex-Mex",
            "Thai",
            "Torshi",
            "Tortillas",
            "American (Traditional)",
            "Traditional Swedish",
            "Trattorie",
            "Turkish",
            "Ukrainian",
            "Uzbek",
            "Vegan",
            "Vegetarian",
            "Venison",
            "Vietnamese",
            "Waffles",
            "Water Stores",
            "Wineries",
            "Wok",
            "Wraps",
            "Yugoslav",
            "Zapiekanka"
    };
    public static final String[] CATEGORY_ALIAS = {
            "acaibowls",
            "afghani",
            "african",
            "andalusian",
            "arabian",
            "argentine",
            "armenian",
            "asianfusion",
            "asturian",
            "australian",
            "austrian",
            "backshop",
            "bagels",
            "baguettes",
            "bakeries",
            "bangladeshi",
            "basque",
            "bavarian",
            "bbq",
            "beer_and_wine",
            "beergarden",
            "beerhall",
            "beisl",
            "belgian",
            "bento",
            "beverage_stores",
            "bistros",
            "blacksea",
            "brasseries",
            "brazilian",
            "breakfast_brunch",
            "breweries",
            "british",
            "bubbletea",
            "buffets",
            "bulgarian",
            "burgers",
            "burmese",
            "butcher",
            "cafes",
            "cafeteria",
            "cajun",
            "cakeshop",
            "cambodian",
            "canteen",
            "caribbean",
            "catalan",
            "cheesesteaks",
            "chicken_wings",
            "chickenshop",
            "chilean",
            "chimneycakes",
            "chinese",
            "churros",
            "cideries",
            "coffee",
            "coffeeroasteries",
            "coffeeteasupplies",
            "comfortfood",
            "convenience",
            "corsican",
            "creperies",
            "csa",
            "cuban",
            "cupcakes",
            "currysausage",
            "customcakes",
            "cypriot",
            "czech",
            "czechslovakian",
            "danish",
            "delicatessen",
            "delis",
            "desserts",
            "diners",
            "dinnertheater",
            "distilleries",
            "diyfood",
            "donairs",
            "donuts",
            "dumplings",
            "eastern_european",
            "eltern_cafes",
            "empanadas",
            "eritrean",
            "ethicgrocery",
            "ethiopian",
            "farmersmarket",
            "filipino",
            "fischbroetchen",
            "fishmonger",
            "fishnchips",
            "flatbread",
            "fondue",
            "food_court",
            "fooddeliveryservices",
            "foodstands",
            "foodtrucks",
            "freiduria",
            "french",
            "friterie",
            "galician",
            "gamemeat",
            "gastropubs",
            "gelato",
            "georgian",
            "german",
            "giblets",
            "gluhwein",
            "gluten_free",
            "gourmet",
            "greek",
            "grocery",
            "guamanian",
            "halal",
            "hawaiian",
            "hawkercentre",
            "heuriger",
            "himalayan",
            "hkcafe",
            "honduran",
            "honey",
            "hotdog",
            "hotdogs",
            "hotpot",
            "hungarian",
            "iberian",
            "icecream",
            "importedfood",
            "indonesian",
            "indpak",
            "international",
            "internetcafe",
            "intlgrocery",
            "irish",
            "island_pub",
            "israeli",
            "italian",
            "japanese",
            "jewish",
            "jpsweets",
            "juicebars",
            "kebab",
            "kiosk",
            "kombucha",
            "kopitiam",
            "korean",
            "kosher",
            "kurdish",
            "laos",
            "laotian",
            "latin",
            "lyonnais",
            "malaysian",
            "meaderies",
            "meatballs",
            "mediterranean",
            "mexican",
            "mideastern",
            "milkbars",
            "milkshakebars",
            "modern_australian",
            "modern_european",
            "mongolian",
            "moroccan",
            "nasilemak",
            "newamerican",
            "newcanadian",
            "newmexican",
            "newzealand",
            "nicaraguan",
            "nightfood",
            "nikkei",
            "noodles",
            "norcinerie",
            "norwegian",
            "opensandwiches",
            "organic_stores",
            "oriental",
            "pakistani",
            "panasian",
            "panzerotti",
            "parma",
            "persian",
            "peruvian",
            "pfcomercial",
            "piadina",
            "pita",
            "pizza",
            "poke",
            "polish",
            "polynesian",
            "popuprestaurants",
            "portuguese",
            "potatoes",
            "poutineries",
            "pretzels",
            "pubfood",
            "raw_food",
            "riceshop",
            "romanian",
            "rotisserie_chicken",
            "russian",
            "salad",
            "salumerie",
            "sandwiches",
            "scandinavian",
            "schnitzel",
            "scottish",
            "seafood",
            "serbocroatian",
            "shavedice",
            "shavedsnow",
            "signature_cuisine",
            "singaporean",
            "slovakian",
            "smokehouse",
            "somali",
            "soulfood",
            "soup",
            "southern",
            "spanish",
            "srilankan",
            "steak",
            "streetvendors",
            "sud_ouest",
            "sugarshacks",
            "supperclubs",
            "sushi",
            "swabian",
            "swedish",
            "swissfood",
            "syrian",
            "tabernas",
            "taiwanese",
            "tapas",
            "tapasmallplates",
            "tavolacalda",
            "tea",
            "tex-mex",
            "thai",
            "torshi",
            "tortillas",
            "tradamerican",
            "traditional_swedish",
            "trattorie",
            "turkish",
            "ukrainian",
            "uzbek",
            "vegan",
            "vegetarian",
            "venison",
            "vietnamese",
            "waffles",
            "waterstores",
            "wineries",
            "wok",
            "wraps",
            "yugoslav",
            "zapiekanka"
    };
}