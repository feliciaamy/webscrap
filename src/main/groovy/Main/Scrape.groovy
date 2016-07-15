package Main

import groovy.transform.Immutable
import groovy.transform.TypeChecked

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by user on 4/7/16.
 */
public class Scrape {

    public static def main(def args) {
        def date = new Date()
        def loc = "Singapore"
        def store = "FairPrice"
        Map items = new HashMap()
        def writer = new StringWriter()
        def output = new File('output.txt')

        def urls = ["http://www.fairprice.com.sg/webapp/wcs/stores/servlet/CategoryDisplay?storeId=10001&parent_category_rn=13518&beginIndex=0&urlRequestType=Base&categoryId=44003&top_category=13502&pageView=grid&catalogId=10051",
                    "http://www.fairprice.com.sg/webapp/wcs/stores/servlet/CategoryDisplay?storeId=10001&parent_category_rn=13518&beginIndex=0&urlRequestType=Base&categoryId=13632&top_category=13502&pageView=grid&catalogId=10051",
                    "http://www.fairprice.com.sg/webapp/wcs/stores/servlet/CategoryDisplay?storeId=10001&parent_category_rn=13518&beginIndex=0&urlRequestType=Base&categoryId=13633&top_category=13502&pageView=grid&catalogId=10051",
                    "http://www.fairprice.com.sg/webapp/wcs/stores/servlet/CategoryDisplay?storeId=10001&parent_category_rn=13518&beginIndex=0&urlRequestType=Base&categoryId=13634&top_category=13502&pageView=grid&catalogId=10051",
                    "http://www.fairprice.com.sg/webapp/wcs/stores/servlet/CategoryDisplay?storeId=10001&parent_category_rn=13518&beginIndex=0&urlRequestType=Base&categoryId=13635&top_category=13502&pageView=grid&catalogId=10051",
                    "http://www.fairprice.com.sg/webapp/wcs/stores/servlet/CategoryDisplay?storeId=10001&parent_category_rn=13518&beginIndex=0&urlRequestType=Base&categoryId=13636&top_category=13502&pageView=grid&catalogId=10051",
                    "http://www.fairprice.com.sg/webapp/wcs/stores/servlet/CategoryDisplay?storeId=10001&parent_category_rn=13518&beginIndex=0&urlRequestType=Base&categoryId=13637&top_category=13502&pageView=grid&catalogId=10051",
                    "http://www.fairprice.com.sg/webapp/wcs/stores/servlet/CategoryDisplay?storeId=10001&parent_category_rn=13518&beginIndex=0&urlRequestType=Base&categoryId=13589&top_category=13502&pageView=grid&catalogId=10051",
                    "http://www.fairprice.com.sg/webapp/wcs/stores/servlet/CategoryDisplay?storeId=10001&parent_category_rn=13518&beginIndex=0&urlRequestType=Base&categoryId=13638&top_category=13502&pageView=grid&catalogId=10051"]

        writer.append("Online Shop;Location;Data Extracted;GTIN/Barcode/UPC;Other ID;Product Name;Brand;Unit Size;Category (multiple expected);Image;Price;Description;Description 2;Country of Origin;Dietary Information;Storage;Nutritional Info;Ingredients\n")
        urls.each {
            def data = new URL(it).getText()
            def takeNextData = false
            String name
            def src
            String category
            def price
            def brand
            def unitSize = "null"

            data.eachLine {
                if (it.contains(" | fairprice") || it.contains(" | FairPrice")) {
                    category = it.replace(" | fairprice", "")
                    category = it.replace(" | FairPrice", "")
                    category = category.trim()
                } else if (it.contains("<a  id=\"catalogEntry_img")) {
                    def http = it.indexOf("http")
                    def af = it.indexOf("\"", http)
                    def url = it.substring(http, af)
                    findDetail(url)
                } else if (it.contains("img_plh_n")) {
                    takeNextData = true
                } else if (it.contains("img alt") && !it.contains("Next Page")) {
                    def first = it.indexOf("\"")
                    def second = it.indexOf("\"", first + 1)
                    if (it.contains("\")\"")) {
                        second = it.indexOf("\"", second + 1)
                        second = it.indexOf("\"", second + 1)
                    }

                    name = it.substring(first + 1, second)
                    name = name.replace("#", "")
                    brand = findBrand(name)
                    def digitIndex
                    if (name.contains("(Cont)") || name.contains("(Container)") || name.contains("(Round/Oval)") ||
                            name.contains("(Asst Colours)") || name.contains("(Oval Small)") || name.contains("(Pieces & Stems)")
                            || name.contains("(Lo-Han-Chai)") || name.contains("(Tall)") || name.contains("(T)") || name.contains("(Can)")
                            || name.contains("(Box)") || name.contains("(B)") || name.contains("(S)") || name.contains("(M)") || name.contains("(L)")
                            || name.contains("(Vegetarian)") || name.contains("(Value Pack)") || name.contains("(Wheat Starch)")
                            || name.contains("(Econ Pack)") || name.contains("(B2G1F)") || name.contains("(Crystal)") || name.contains("(1PC)")
                            || name.contains("(120 SQFT)") || name.contains("(240 SQFT)") || name.contains("(Cut)") || name.contains("(Sheet)")
                            || name.contains("(LK)") || name.contains("(Tau Chiam)") || name.contains("(Tai Ann)") || name.contains("(High Quality)")
                            || name.contains("(Thai Hom Mali)") || name.contains("(Small)") || name.contains("(PUMP)") || name.contains("(Low Fat)")
                            || name.contains("(Pyramid Tea Bags)") || name.contains("(100 Teabags)") || name.contains("(20Sachets)")
                            || name.contains("(No Added Sugar)") || name.contains("(No Sugar)") || name.contains("(18 Sachets)")
                            || name.contains("(CTN)") || name.contains("(Manufactured In SG)") || name.contains("(6S)")
                            || name.contains("(Fat Free)") || name.contains("(Low Fat)") || name.contains("(4S)") || name.contains("(Limited Edt)")) {
                        def rest = name.substring(name.indexOf(")"))
                        name = name.substring(0, name.indexOf(")") + 1)
                        if ((digitIndex = findDigit(rest)) != -1) {
                            unitSize = rest.substring(digitIndex)
                        }
                    } else if (name.contains("3in1") || name.contains("2in1") || name.contains("3 in 1") || name.contains("2 in 1")
                            || name.contains("3In1") || name.contains("2In1") || name.contains("5in1") || name.contains("212in1") || name.contains("2-IN-1")) {
                        def rest = name.substring(name.indexOf("1") + 1)
                        name = name.substring(0, name.indexOf("1") + 1)
                        unitSize = rest.substring(findDigit(rest))
                    } else if (name.contains("100%") || name.contains("90%") || name.contains("80%") || name.contains("70%") || name.contains("60%")
                            || name.contains("50%") || name.contains("40%") || name.contains("30%") || name.contains("20%") || name.contains("10%") || name.contains("0%")) {
                        def digit = name.charAt(findDigit(name))
                        def rest = name.substring(name.indexOf("%") + 1)
                        name = name.substring(0, name.indexOf("%") + 1)
                        unitSize = rest.substring(findDigit(rest))
                    } else if ((digitIndex = findDigit(name)) != -1) {
                        if (name.contains("365 ")) {
                            def temp = name.replace("365 ", "")
                            digitIndex = findDigit(temp) + 4
                        }
                        unitSize = name.substring(digitIndex)
                        name = name.substring(0, digitIndex)
                    }
                } else if (it.contains("pl_lst_rt")) {
                    takeNextData = true
                } else if (it.contains("\$") && takeNextData) {
                    price = it.replaceAll("\\s+", "")
                    takeNextData = false
                } else if (takeNextData && it.contains("http")) {
                    src = it
                    takeNextData = false
                } else if (it.contains("Add to Cart")) {
                    writer.append("$store;$loc;$date;null;null;$name;$brand;$unitSize;$category;$src;$price;null;null;null;null;null;null;null\n")
                }
            }
        }


        PrintWriter pw = new PrintWriter(output)
        pw.write(writer.toString())
        pw.close()
    }

    private static int findDigit(String str) {
        def first = -1
        Pattern pattern = Pattern.compile("^\\D*(\\d)");
        Matcher m = pattern.compile("[0-9(]").matcher(str);
        if (m.find()) {
            first = m.start(0)
        }
        return first
    }

    private static String findBrand(String name) {
        List<String> a = ["A&W", "A & W", "AAA", "A Jemima", "Adabi", "Adams", "Ah Lai", "Aik Cheong", "Ajinomoto", "Alce Nero", "Allswell Drink", "Allswell", "Alpen", "Amocan",
                          "Anlene", "Anmum", "Anzen", "Aqua Mountain", "Aquarius", "Arm & Hammer", "Aunt Jemima", "Auspro", "Ay Chiwawa", "Ayam", "Ayam Brand"]
        List<String> b = ["Baba's", "Bake King", "Bamboo Hill", "Baxters", "Best Foods", "Bertolli", "Bestal", "Betty Crocker",
                          "Betty Crockers", "Bird's", "Boat Brand", "Boh Cameron", "Boklunder Bockwurst", "Bolero", "Bonchoco", "Borges", "Budget", "Bull Dog/Chan Kong Thye", "Butterfingers"]
        List<String> c = ["Cabbage", "Cadbury", "Carnation", "Capilano", "Captain Oats", "Carrefour", "Camel", "Campbell's", "Cintan", "Cixin",
                          "Chefking", "Chief Brand", "Chilli Brand", "Chio", "Chupa Chups", "CJ Beksul", "Claypot", "Crab", "Co.Op", "Co.op", "Coco Life", "Cock Brand",
                          "Coffeehock", "Coffeeking", "Coffeemix", "Colavita", "Companion", "Corntos", "Cowhead"]
        List<String> d = ["Dahfa", "Dahlia", "Daisy", "Daribell", "Dark Amber MacDonald's", "Dasani", "De Nigris", "Delfi", "Del Monte",
                          "Delhaize", "Delmonte", "Dilmah", "Dragonfly", "Drinho", "Double Decker", "Double FP", "Double Fp", "Duck Brand", "Dutch Mill"]
        List<String> e = ["ecoBrown's", "Emborg", "Erawan Elephant"]
        List<String> f = ["F&N", "Fanta", "Farmer Brand", "Farmhouse", "FairPrice", "Fairprice", "Farmland", "Feng Zheng", "Fernleaf", "FLS",
                          "Flying Man", "Frezfruta", "Friso Mum", "FrisoMum", "Frisomum", "Food People", "Fortune"]
        List<String> g = ["Gingen", "Glaceau", "Glico", "Green's", "Greenland", "Greenmax", "Gulong", "Gold Kili", "Gold Ribbon", "Golden", "Golden Bridge",
                          "Golden Light", "Goldkili", "Goldyna"]
        List<String> h = ["H-Two-O", "Harada Yabukita", "Haruma", "Harvest Fields", "Harvey", "Happy Grass", "Heaven & Earth", "Heinz", "Hershey's", "Highway", "Hollyfarms",
                          "Home Gourmet", "HomeProud", "Honegar", "Hosei", "Hosen", "Horlicks", "Hunt's"]
        List<String> i = ["Ice Cool", "Ice Mountain", "Idahoan", "Ikan Brand", "Indocafe", "IXL Jam"]
        List<String> j = ["Jack & Jill", "Jelly Joy", "Jilin", "Joy", "JW"]
        List<String> k = ["Kara", "Kellogg's", "Kelly's", "Kerk", "Kewpie", "Kimball", "Kinder", "King Rooster", "Kit Kat",
                          "Knife Br", "Knife Brand", "Knorr", "Kraft", "Koka", "Khong Guan", "Kokuho"]
        List<String> l = ["Lady's Choice", "Lipton", "Lee Kum Kee ", "Leezen", "Libby's", "Ligo", "Lindt", "Lipco", "London", "Lotte", "Lowan"]
        List<String> m = ["M & M's", "M&M's", "Maggi", "Maling", "Mamee", "Magnolia", "Marmite", "Marigold", "Marigold", "Max's", "Maxtea", "Mccormick", "Medella", "Mei Way", "Meiji", "Melrose", "Megah",
                          "Merito", "Mili", "Milo", "Milkmaid", "Mirinda", "Mister Potato", "Mitrphol", "Mr Tea", "Mona", "Mountain Dew", "Mountains", "Montvergin", "Mug", "Myojo"]
        List<String> n = ["Narcissus", "Naspac", "Natur-A", "Natural Park", "Naturel", "Naturel Organic", "Nature's Path Org", "Nature Valley", "Nature's Wonders", "Nat Valley",
                          "Nespray", "Nestle", "New Moon", "Nissin", "Nongshim", "Nutella"]
        List<String> o = ["Obento", "Oishi", "Oki", "Old Town", "Oldelpaso", "Origins", "Ovaltine", "Owl"]
        List<String> p = ["Pepsi", "Pocari Sweat", "Pokka", "Pai Chia Chen", "Pagoda", "Pasar", "Paul's", "Peacock", "Perrier", "Peter Pan", "Ping Pong", "Planta",
                          "Pleasant", "Po Po", "Polar", "Polleney", "Poppin", "Post", "Prego", "Pretty Jade Lady", "Prima", "Prima Taste", "Pureharvest"]
        List<String> q = ["QBB", "Quaker"]
        List<String> r = ["Ravika", "Red Bull", "Red Man", "Ribena", "Rice Field", "Rickshaw", "Ricola", "Ritter Sport", "Rocky Mountain", "Royal Umbrella"]
        List<String> s = ["S&W", "S & W", "Sanwa", "SAF", "Sakura", "San Remo", "Sea Dyke", "Seasons", "Shanghai Maling", "Shokaku", "Sichuan", "Silver Bird",
                          "Sing Long", "Singlong", "SIS", "Skippy", "Smuckers", "SongHe", "Songhe", "Sotong", "Soyjoy", "Sprite", "Star Lion",
                          "Streamline", "Storck", "Sun Kee", "Sunmaid", "Sun Brand", "Sunbeam", "Sunflower", "Sunfresh", "SUNSWEET", "Super", "Swallow",
                          "Swan (Tai Ann)", "Swan Tai Ann", "Swanson", "Sweet Nature"]
        List<String> t = ["Tai Hua", "Tai Sun", "Taikoo", "Taisun", "Tarami", "Tan Ngan Lo", "Tango", "Tao Kaenoi", "Taylor", "Teapot", "Temasek", "Tesco Everyday Value", "Tesco",
                          "Three Leg", "Tiger", "Triko", "Toblerone", "Tong Garden", "Tulip"]
        List<String> u = ["UFC", "Uncle Tobys", "Unisoy"]
        List<String> v = ["V-Soy", "Veflower", "Venola", "Vicks", "Victory", "Vitasoy"]
        List<String> w = ["Want Want Senbei", "Wai Wai", "Welch's", "Windmill", "White Wings", "Wholesome", "Wrigley's", "Woh Hup", "Wong Coco"]
        List<String> x = new ArrayList<String>()
        List<String> y = ["Yeo's", "Yifon"]
        List<String> z = new ArrayList<String>()
        List<String> digit = ["100 Plus", "365", "7D", "7Up"]

        Map<String, List<String>> nameMapping = ["A": a, "B": b, "C": c, "D": d, "E": e, "F": f, "G": g, "H": h, "I": i, "J": j, "K": k,
                                                 "L": l, "M": m, "N": n, "O": o, "P": p, "Q": q, "R": r, "S": s, "T": t, "U": u, "V": v,
                                                 "W": w, "X": x, "Y": y, "Z": z, "digit": digit]
        String firstChar = name.charAt(0).toUpperCase()
        String brand
        if (("1234567890").contains("$firstChar")) {
            firstChar = 'digit'
        }
        for (String it : nameMapping.get(firstChar)) {
            if (name.contains(it)) {
                brand = it
                return brand
            }
        }

        println("Couldn't get the brand, would you type the brand: ")
        return System.in.newReader().readLine()
    }

    @TypeChecked
    private static String findDetail(String url) {
        println(url)
        String data = new URL(url).getText()
        def getDesc = false
        def getIngredient = false
        def ingredient = ""
        def getPrepInfo = false
        def preparation_info = ""
        def getCountry = false
        def country = ""
        def getDietaryInfo = false
        def dietaryInfo = ""
        def getStorage = false
        def storage = ""
        def getSpecialRemark = false
        def specialRemark = ""
        def detail = []
        data.eachLine {
            String line = it
            line = line.trim()
            line = line.replaceAll("\t", "")
            line = line.replace("<p>", "").replace("</p>", "")
            switch (line) {
                case { line.contains("\"dt_txt_wrp\"") }:
                    getDesc = true
                    break
                case { getDesc && line.contains("<br>") }:
                    detail = line.split("<br>|</br>")
                    getDesc = false
                    break
                case { line.contains("Ingredients:") }:
                    getIngredient = true
                    break
                case { getIngredient && !line.contains("<") && !line.equals("") }:
                    ingredient = line.trim()
                    getIngredient = false
                    break
                case { line.contains("Preparation Info:") }:
                    getPrepInfo = true
                    break
                case { getPrepInfo && !line.contains("<") && !line.equals("") }:
                    preparation_info = line.trim()
                    getPrepInfo = false
                    break
                case { line.contains("Country Of Origin:") }:
                    "country"
                    getCountry = true
                    break
                case { getCountry && !line.contains("<") && !line.equals("") }:
                    country = line.trim()
                    getCountry = false
                    break
                case { line.contains("Dietary Information:") }:
                    getDietaryInfo = true
                    break
                case { getDietaryInfo && !line.contains("<") && !line.equals("") }:
                    dietaryInfo = line.trim()
                    getDietaryInfo = false
                    break
                case { line.contains("Special Remarks:") }:
                    getSpecialRemark = true
                    break
                case { getSpecialRemark && !line.contains("<") && !line.equals("") }:
                    specialRemark = line.trim()
                    getSpecialRemark = false
                    break
            }
        }
        println(detail)
        println(ingredient)
        println(preparation_info)
        println(country)
        println(dietaryInfo)
        println(specialRemark)
        println(storage)

    }

    @Immutable
    class Item {
        String shop
        String location
        Date dataExtracted
        String barcode
        String otherID
        String name
        String brand
        String unitSize
        String category
        String imageLink
        String price
        String detail
        String ingredient
        String prepInfo
        String country
        String specialRemark
        String storage
    }
}
