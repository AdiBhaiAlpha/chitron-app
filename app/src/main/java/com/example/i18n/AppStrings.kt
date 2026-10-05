package com.example.i18n

enum class AppLanguage(val code: String, val label: String) {
    EN("en", "EN"),
    BN("bn", "বাং")
}

object AppStrings {
    private val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    fun formatNumber(value: Int, lang: AppLanguage): String {
        val raw = value.toString()
        if (lang != AppLanguage.BN) return raw
        return buildString {
            for (ch in raw) {
                if (ch in '0'..'9') append(bnDigits[ch - '0']) else append(ch)
            }
        }
    }

    fun formatReadTime(minutes: Int, lang: AppLanguage): String {
        val mins = minutes.coerceAtLeast(1)
        return if (lang == AppLanguage.BN) {
            "${formatNumber(mins, lang)} মিনিট পড়ার সময়"
        } else {
            "$mins min read"
        }
    }

    private val enMap = mapOf(
        "nav.home" to "Home",
        "nav.about" to "About",
        "nav.writing" to "Writing",
        "nav.gallery" to "Gallery",
        "nav.admin" to "Admin",
        "home.heroTitle" to "Chitron Bhattacharjee",
        "home.heroDesc" to "AI developer, programmer, designer and writer from Bangladesh. This is my personal archive — notes, ideas, experiments and things worth remembering.",
        "home.ctaPrimary" to "Read Writing",
        "home.ctaSecondary" to "About Me",
        "home.latestWriting" to "Latest Writing",
        "home.viewAll" to "View all →",
        "sidebar.aboutTitle" to "About Me",
        "sidebar.aboutRole" to "AI Developer & Writer",
        "sidebar.aboutBio" to "Building conversational AI systems, web tools, and writing on technology.",
        "sidebar.viewProfile" to "Full Profile →",
        "sidebar.trendingTitle" to "Trending Articles",
        "sidebar.categoriesTitle" to "Explore Topics",
        "writing.title" to "Writing & Articles",
        "writing.searchPlaceholder" to "Search articles by title...",
        "writing.allCategories" to "All categories",
        "writing.newestFirst" to "Newest first",
        "writing.oldestFirst" to "Oldest first",
        "writing.noPosts" to "No posts found.",
        "post.back" to "Back to Writing",
        "post.prev" to "← Previous",
        "post.next" to "Next →",
        "gallery.title" to "Photo Gallery",
        "gallery.subtitle" to "Visual moments, captures, workspaces, and photographic memories.",
        "gallery.all" to "All Photos",
        "gallery.empty" to "No photos found.",
        "gallery.viewFullscreen" to "View in full-screen",
        "gallery.photosCount" to "photos",
        "about.headline" to "Hi, I’m Chitron Bhattacharjee.",
        "about.shortBio1" to "I’m an AI developer, programmer, and writer from Bangladesh. I enjoy building things with technology, especially AI-powered systems, web applications, and tools that solve real problems in a simple way.",
        "about.shortBio2" to "I’m always interested in learning how things work behind the scenes and turning ideas into something people can actually use.",
        "about.secAbout" to "About",
        "about.bio1" to "I work with modern web technologies and enjoy experimenting with AI, automation, and conversational systems. Most of my time goes into building projects, improving my skills, and exploring new ideas in technology.",
        "about.bio2" to "I also enjoy writing. Sometimes I write about technology, sometimes about ideas and experiences, and sometimes simply to put thoughts into words.",
        "about.secRoles" to "What I Do",
        "about.secProjects" to "Selected Projects",
        "about.secSkills" to "Technology",
        "about.skillsIntro" to "I regularly work with technologies such as:",
        "about.skillsOutro" to "I’m particularly interested in backend systems, AI integration, APIs, automation, and building fast web applications.",
        "about.secInterests" to "Interests",
        "about.interestsIntro" to "My main interests include:",
        "about.secWriting" to "Writing",
        "about.writingDesc" to "Writing gives me another way to explore and share ideas. Here you'll find a mix of reflective writing, technical notes, experiments, and thoughts about technology and the things I learn while building.",
        "about.readWriting" to "Read my writing →",
        "about.secPhilosophy" to "Philosophy",
        "about.philosophy1" to "I believe good software does not need to be unnecessarily complicated. I prefer things that are simple, fast, practical, and easy to understand.",
        "about.philosophy2" to "Whether I’m building a small tool or working on a larger project, I try to focus on making it useful first. Technology should solve problems, not create more of them.",
        "about.secCurrently" to "Currently",
        "about.currently" to "Right now, I’m working on AI-related projects, conversational systems, and personal web platforms. I’m also continuing to learn and experiment with new technologies as I build.",
        "about.secContact" to "Contact",
        "about.contactIntro" to "You can find me on GitHub and social media. Feel free to explore my work, read what I write, or connect with me online.",
        "chat.fabLabel" to "Message Me",
        "chat.title" to "Chitron Bhattacharjee",
        "chat.subtitleOnline" to "Available • Replies in real-time",
        "chat.welcomeTitle" to "Direct Message",
        "chat.welcomeDesc" to "Send a message anytime — no account or login needed. Your conversation stays saved on this device so you can check back for replies later.",
        "chat.placeholder" to "Write a message...",
        "chat.send" to "Send",
        "chat.visitorNameLabel" to "Your Name (Optional)",
        "chat.visitorNamePlaceholder" to "Anonymous Visitor",
        "chat.starter1" to "Hi, I need a website or web app.",
        "chat.starter2" to "I would like to discuss an AI project.",
        "chat.starter3" to "Hello! Just wanted to connect."
    )

    private val bnMap = mapOf(
        "nav.home" to "হোম",
        "nav.about" to "পরিচিতি",
        "nav.writing" to "লেখালেখি",
        "nav.gallery" to "গ্যালারি",
        "nav.admin" to "অ্যাডমিন",
        "home.heroTitle" to "চিত্রন ভট্টাচার্য",
        "home.heroDesc" to "বাংলাদেশের একজন এআই ডেভেলপার, প্রোগ্রামার ও লেখক। এটি আমার ব্যক্তিগত ডিজিটাল আর্কাইভ — নোট, ভাবনা, প্রযুক্তিগত পরীক্ষা-নিরীক্ষা ও স্মৃতি।",
        "home.ctaPrimary" to "লেখালেখি পড়ুন",
        "home.ctaSecondary" to "আমার সম্পর্কে",
        "home.latestWriting" to "সাম্প্রতিক লেখা",
        "home.viewAll" to "সব দেখুন →",
        "sidebar.aboutTitle" to "আমার সম্পর্কে",
        "sidebar.aboutRole" to "এআই ডেভেলপার ও লেখক",
        "sidebar.aboutBio" to "এআই সিস্টেম, ওয়েব টুলস তৈরি এবং প্রযুক্তি বিষয়ক লেখালেখি করি।",
        "sidebar.viewProfile" to "সম্পূর্ণ প্রোফাইল →",
        "sidebar.trendingTitle" to "জনপ্রিয় নিবন্ধ",
        "sidebar.categoriesTitle" to "ক্যাটাগরি সমূহ",
        "writing.title" to "লেখালেখি ও প্রবন্ধ",
        "writing.searchPlaceholder" to "শিরোনাম দিয়ে নিবন্ধ খুঁজুন...",
        "writing.allCategories" to "সব ক্যাটাগরি",
        "writing.newestFirst" to "সর্বশেষ আগে",
        "writing.oldestFirst" to "প্রাচীনতম আগে",
        "writing.noPosts" to "কোনো নিবন্ধ পাওয়া যায়নি।",
        "post.back" to "লেখালেখিতে ফিরে যান",
        "post.prev" to "← পূর্ববর্তী",
        "post.next" to "পরবর্তী →",
        "gallery.title" to "ফটোগ্রাফি ও ছবি",
        "gallery.subtitle" to "দৃশ্যমান মুহূর্ত, কর্মক্ষেত্র, এবং ফটোগ্রাফি সংকলন।",
        "gallery.all" to "সব ছবি",
        "gallery.empty" to "কোনো ছবি পাওয়া যায়নি।",
        "gallery.viewFullscreen" to "ফুলস্ক্রিনে দেখুন",
        "gallery.photosCount" to "টি ছবি",
        "about.headline" to "নমস্কার, আমি চিত্রন ভট্টাচার্য।",
        "about.shortBio1" to "আমি বাংলাদেশের একজন এআই ডেভেলপার, প্রোগ্রামার ও লেখক। প্রযুক্তির সাহায্যে প্রয়োজনীয় জিনিস তৈরি করতে ভালোবাসি—বিশেষ করে এআই-চালিত সিস্টেম, ওয়েব অ্যাপ্লিকেশন এবং বাস্তব সমস্যার সহজ ও কার্যকর সমাধান।",
        "about.shortBio2" to "কোনো সিস্টেম পর্দার আড়ালে কীভাবে কাজ করে তা অনুসন্ধান করা এবং বিভিন্ন আইডিয়াকে মানুষের ব্যবহারযোগ্য টুলে রূপ দেওয়ার প্রতি আমার সবসময় গভীর আগ্রহ।",
        "about.secAbout" to "পরিচিতি",
        "about.bio1" to "আমি আধুনিক ওয়েব প্রযুক্তি নিয়ে কাজ করি এবং এআই, অটোমেশন ও কনভার্সেশনাল সিস্টেম নিয়ে পরীক্ষা-নিরীক্ষা করতে ভালোবাসি। আমার বেশিরভাগ সময় কাটে বিভিন্ন প্রজেক্ট তৈরি, নিজের দক্ষতা উন্নয়ন এবং প্রযুক্তির নতুন সম্ভাবনা অন্বেষণে।",
        "about.bio2" to "পাশাপাশি আমি লিখতে পছন্দ করি। কখনো প্রযুক্তি নিয়ে, কখনো নানা আইডিয়া ও অভিজ্ঞতা নিয়ে, আবার কখনো কেবল মনের ভাবনাগুলোকে শব্দে ফুটিয়ে তুলতে লিখি।",
        "about.secRoles" to "আমি যা করি",
        "about.secProjects" to "নির্বাচিত প্রজেক্ট",
        "about.secSkills" to "প্রযুক্তি",
        "about.skillsIntro" to "আমি নিয়মিত যে প্রযুক্তিগুলো নিয়ে কাজ করি:",
        "about.skillsOutro" to "বিশেষ করে ব্যাকএন্ড আর্কিটেকচার, এআই ইন্টিগ্রেশন, এপিআই, অটোমেশন এবং উচ্চগতির ওয়েব অ্যাপ্লিকেশন নির্মাণে আমার গভীর আগ্রহ রয়েছে।",
        "about.secInterests" to "আগ্রহের বিষয়",
        "about.interestsIntro" to "আমার প্রধান আগ্রহের ক্ষেত্রসমূহ:",
        "about.secWriting" to "লেখালেখি",
        "about.writingDesc" to "লেখালেখি আমাকে নিজের চিন্তাভাবনা অন্বেষণ ও ভাগ করে নেওয়ার অনন্য সুযোগ দেয়। এখানে পাবেন মননশীল প্রবন্ধ, টেকনিক্যাল নোটস, বিভিন্ন এক্সপেরিমেন্ট এবং কাজ করার অভিজ্ঞতা থেকে শেখা নানা বিষয়ের প্রতিফলন।",
        "about.readWriting" to "আমার লেখা পড়ুন →",
        "about.secPhilosophy" to "দর্শন",
        "about.philosophy1" to "আমি বিশ্বাস করি ভালো সফটওয়্যার অপ্রয়োজনীয়ভাবে জটিল হওয়ার প্রয়োজন নেই। যা সাধারণ, দ্রুতগতিসম্পন্ন, ব্যবহারিক এবং সহজে বোধগম্য—আমি সেটাই পছন্দ করি।",
        "about.philosophy2" to "একটি ছোট টুল হোক কিংবা বড় কোনো প্রজেক্ট, আমি সবসময় সেটিকে প্রথমত মানুষের জন্য কার্যকর ও সহায়ক করে তোলার ওপর জোর দিই। প্রযুক্তির উদ্দেশ্য সমস্যা সমাধান করা, নতুন জটিলতা সৃষ্টি করা নয়।",
        "about.secCurrently" to "বর্তমানে",
        "about.currently" to "বর্তমানে আমি এআই-সম্পর্কিত প্রজেক্ট, কনভার্সেশনাল সিস্টেম এবং ব্যক্তিগত ওয়েব প্ল্যাটফর্ম নিয়ে কাজ করছি। পাশাপাশি কাজ করতে করতে নতুন প্রযুক্তি শেখা ও পরীক্ষা-নিরীক্ষা অব্যাহত রেখেছি।",
        "about.secContact" to "যোগাযোগ",
        "about.contactIntro" to "GitHub এবং সোশ্যাল মিডিয়ায় আমাকে খুঁজে পাবেন। আমার কাজ দেখতে পারেন, লেখা পড়তে পারেন কিংবা অনলাইনে আমার সাথে সরাসরি যুক্ত হতে পারেন।",
        "chat.fabLabel" to "বার্তা পাঠান",
        "chat.title" to "চিত্রন ভট্টাচার্য",
        "chat.subtitleOnline" to "সক্রিয় • রিয়েল-টাইম উত্তর",
        "chat.welcomeTitle" to "সরাসরি বার্তা পাঠান",
        "chat.welcomeDesc" to "যেকোনো সময় বার্তা পাঠান — কোনো অ্যাকাউন্ট বা লগইনের প্রয়োজন নেই। আপনার কথোপকথন এই ডিভাইসে সংরক্ষিত থাকবে, ফলে পরে এসে উত্তর দেখতে পাবেন।",
        "chat.placeholder" to "আপনার বার্তা লিখুন...",
        "chat.send" to "পাঠান",
        "chat.visitorNameLabel" to "আপনার নাম (ঐচ্ছিক)",
        "chat.visitorNamePlaceholder" to "অতিথি ভিজিটর",
        "chat.starter1" to "হ্যালো, আমার একটি ওয়েবসাইট প্রয়োজন।",
        "chat.starter2" to "আমি একটি এআই প্রজেক্ট নিয়ে আলোচনা করতে চাই।",
        "chat.starter3" to "নমস্কার! আপনার সাথে পরিচিত হতে চাই।"
    )

    val rolesEn = listOf(
        "Build AI-powered applications and conversational systems",
        "Develop full-stack web applications",
        "Work with JavaScript, Node.js, PHP, and modern web technologies",
        "Design clean and practical user interfaces",
        "Write about technology, ideas, and personal thoughts"
    )

    val rolesBn = listOf(
        "এআই-চালিত অ্যাপ্লিকেশন ও কনভার্সেশনাল সিস্টেম নির্মাণ",
        "ফুল-স্ট্যাক ওয়েব অ্যাপ্লিকেশন তৈরি",
        "জাভাস্ক্রিপ্ট, নোড.জেএস, পিএইচপি ও আধুনিক ওয়েব প্রযুক্তিতে কাজ",
        "সহজ, সুন্দর ও ব্যবহারিক ইউজার ইন্টারফেস ডিজাইন",
        "প্রযুক্তি, ভাবনা ও ব্যক্তিগত দৃষ্টিভঙ্গি নিয়ে লেখালেখি"
    )

    val interestsEn = listOf(
        "Artificial Intelligence",
        "Programming",
        "Web Development",
        "Conversational Systems",
        "UI/UX Design",
        "Writing",
        "Software Architecture"
    )

    val interestsBn = listOf(
        "কৃত্রিম বুদ্ধিমত্তা (Artificial Intelligence)",
        "প্রোগ্রামিং (Programming)",
        "ওয়েব ডেভেলপমেন্ট (Web Development)",
        "কনভার্সেশনাল সিস্টেম (Conversational Systems)",
        "ইউআই/ইউএক্স ডিজাইন (UI/UX Design)",
        "লেখালেখি ও প্রবন্ধ (Writing)",
        "সফটওয়্যার আর্কিটেকচার (Software Architecture)"
    )

    fun t(key: String, lang: AppLanguage): String {
        val dict = if (lang == AppLanguage.BN) bnMap else enMap
        return dict[key] ?: enMap[key] ?: key
    }
}
