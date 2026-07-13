package com.ovi.codetrack.shared.domain.model

import com.ovi.codetrack.shared.presentation.model.Difficulty

data class RoadmapProblem(
    val id: Int,
    val title: String,
    val difficulty: Difficulty,
    val tags: List<String>,
    val url: String
)

object ProblemRoadmap {
    val problems = listOf(
        // Easy
        RoadmapProblem(1, "Two Sum", Difficulty.EASY, listOf("Array", "Hash Table"), "https://leetcode.com/problems/two-sum/"),
        RoadmapProblem(20, "Valid Parentheses", Difficulty.EASY, listOf("String", "Stack"), "https://leetcode.com/problems/valid-parentheses/"),
        RoadmapProblem(21, "Merge Two Sorted Lists", Difficulty.EASY, listOf("Linked List", "Recursion"), "https://leetcode.com/problems/merge-two-sorted-lists/"),
        RoadmapProblem(121, "Best Time to Buy and Sell Stock", Difficulty.EASY, listOf("Array", "Dynamic Programming"), "https://leetcode.com/problems/best-time-to-buy-and-sell-stock/"),
        RoadmapProblem(125, "Valid Palindrome", Difficulty.EASY, listOf("Two Pointers", "String"), "https://leetcode.com/problems/valid-palindrome/"),
        RoadmapProblem(226, "Invert Binary Tree", Difficulty.EASY, listOf("Tree", "Binary Tree"), "https://leetcode.com/problems/invert-binary-tree/"),
        RoadmapProblem(242, "Valid Anagram", Difficulty.EASY, listOf("Hash Table", "String"), "https://leetcode.com/problems/valid-anagram/"),
        RoadmapProblem(704, "Binary Search", Difficulty.EASY, listOf("Array", "Binary Search"), "https://leetcode.com/problems/binary-search/"),
        
        // Medium
        RoadmapProblem(3, "Longest Substring Without Repeating Characters", Difficulty.MEDIUM, listOf("Hash Table", "Sliding Window"), "https://leetcode.com/problems/longest-substring-without-repeating-characters/"),
        RoadmapProblem(11, "Container With Most Water", Difficulty.MEDIUM, listOf("Array", "Two Pointers"), "https://leetcode.com/problems/container-with-most-water/"),
        RoadmapProblem(15, "3Sum", Difficulty.MEDIUM, listOf("Array", "Two Pointers"), "https://leetcode.com/problems/3sum/"),
        RoadmapProblem(33, "Search in Rotated Sorted Array", Difficulty.MEDIUM, listOf("Array", "Binary Search"), "https://leetcode.com/problems/search-in-rotated-sorted-array/"),
        RoadmapProblem(39, "Combination Sum", Difficulty.MEDIUM, listOf("Array", "Backtracking"), "https://leetcode.com/problems/combination-sum/"),
        RoadmapProblem(46, "Permutations", Difficulty.MEDIUM, listOf("Array", "Backtracking"), "https://leetcode.com/problems/permutations/"),
        RoadmapProblem(48, "Rotate Image", Difficulty.MEDIUM, listOf("Array", "Math", "Matrix"), "https://leetcode.com/problems/rotate-image/"),
        RoadmapProblem(49, "Group Anagrams", Difficulty.MEDIUM, listOf("Array", "Hash Table", "String"), "https://leetcode.com/problems/group-anagrams/"),
        RoadmapProblem(53, "Maximum Subarray", Difficulty.MEDIUM, listOf("Array", "Divide and Conquer", "Dynamic Programming"), "https://leetcode.com/problems/maximum-subarray/"),
        RoadmapProblem(56, "Merge Intervals", Difficulty.MEDIUM, listOf("Array", "Sorting"), "https://leetcode.com/problems/merge-intervals/"),
        RoadmapProblem(98, "Validate Binary Search Tree", Difficulty.MEDIUM, listOf("Tree", "Depth-First Search", "Binary Search Tree"), "https://leetcode.com/problems/validate-binary-search-tree/"),
        RoadmapProblem(102, "Binary Tree Level Order Traversal", Difficulty.MEDIUM, listOf("Tree", "Breadth-First Search", "Binary Tree"), "https://leetcode.com/problems/binary-tree-level-order-traversal/"),
        
        // Hard
        RoadmapProblem(4, "Median of Two Sorted Arrays", Difficulty.HARD, listOf("Array", "Binary Search", "Divide and Conquer"), "https://leetcode.com/problems/median-of-two-sorted-arrays/"),
        RoadmapProblem(23, "Merge k Sorted Lists", Difficulty.HARD, listOf("Linked List", "Divide and Conquer", "Heap"), "https://leetcode.com/problems/merge-k-sorted-lists/"),
        RoadmapProblem(42, "Trapping Rain Water", Difficulty.HARD, listOf("Array", "Two Pointers", "Dynamic Programming", "Stack"), "https://leetcode.com/problems/trapping-rain-water/"),
        RoadmapProblem(76, "Minimum Window Substring", Difficulty.HARD, listOf("Hash Table", "String", "Sliding Window"), "https://leetcode.com/problems/minimum-window-substring/"),
        RoadmapProblem(295, "Find Median from Data Stream", Difficulty.HARD, listOf("Two Pointers", "Design", "Sorting", "Heap"), "https://leetcode.com/problems/find-median-from-data-stream/")
    )
}
