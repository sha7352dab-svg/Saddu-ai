package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AssistantMode
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.SurfaceBorderDark
import com.example.ui.theme.SurfaceContainerDark
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.VioletGlow

data class QuickPrompt(
    val emoji: String,
    val title: String,
    val prompt: String,
    val tag: String
)

@Composable
fun QuickPromptsView(
    mode: AssistantMode,
    onSelectPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val prompts = when (mode) {
        AssistantMode.ANIMATION -> listOf(
            QuickPrompt("🌌", "Cosmic Vortex", "Write an interactive 60fps Cosmic Particle Vortex animation in Jetpack Compose Canvas with touch attraction.", "anim_galaxy"),
            QuickPrompt("⚡", "Neon Pulse & Waves", "Create a futuristic Cyberpunk Neon Audio Wave Visualizer animation in Compose Canvas.", "anim_pulse"),
            QuickPrompt("🎆", "Particle Fireworks", "How to build a bursting particle firework explosion effect with velocity physics in Compose?", "anim_fireworks"),
            QuickPrompt("🌊", "Fluid Wave Loader", "Write a soothing animated liquid gradient wave loading bar with smooth sine curves in Compose.", "anim_liquid")
        )
        AssistantMode.CODING -> listOf(
            QuickPrompt("💻", "Compose Animation", "Write a clean Kotlin Jetpack Compose shimmer loading animation with code comments.", "coding_shimmer"),
            QuickPrompt("🐞", "Debug Code", "Help me identify memory leaks in Android ViewModels and Coroutines.", "coding_leak"),
            QuickPrompt("🏗️", "Clean Architecture", "Explain MVI vs MVVM architecture in modern Android with a sample Flow.", "coding_arch"),
            QuickPrompt("⚡", "Regex Master", "Write a regex to extract emails and phone numbers with test examples.", "coding_regex")
        )
        AssistantMode.CREATIVE -> listOf(
            QuickPrompt("✍️", "Follow-up Email", "Draft a polite and impactful follow-up email after a job interview.", "creative_email"),
            QuickPrompt("📖", "Sci-Fi Story", "Write a gripping 3-paragraph opening for a sci-fi mystery set in Neo-Mumbai 2099.", "creative_story"),
            QuickPrompt("🎯", "Brand Taglines", "Give me 5 catchy modern taglines for an eco-friendly tech startup.", "creative_tagline"),
            QuickPrompt("🎭", "Poetic Verses", "Write a soulful poem about chasing dreams amidst night city lights.", "creative_poem")
        )
        AssistantMode.LOGIC_MATH -> listOf(
            QuickPrompt("🧠", "Brain Teaser", "Give me a thought-provoking logic riddle with hints.", "logic_riddle"),
            QuickPrompt("📐", "Calculus & Limits", "Explain the concept of derivative intuitively with real-life analogies.", "logic_math"),
            QuickPrompt("📊", "Bayes' Theorem", "Explain Bayes' theorem with a simple everyday diagnostic example.", "logic_bayes"),
            QuickPrompt("♟️", "Strategy Game", "What are the fundamental principles of positional play in chess?", "logic_chess")
        )
        AssistantMode.HINGLISH_DOST -> listOf(
            QuickPrompt("🙏", "Namaste Saddu!", "Namaste Saddu! Aap kaun ho aur meri help kaise kar sakte ho?", "hinglish_intro"),
            QuickPrompt("☕", "Desi Chai Pe Charcha", "Mujhe coding seekhne ka best practical roadmap batao step-by-step.", "hinglish_roadmap"),
            QuickPrompt("🚀", "Time Management", "Bro, din bhar procrastinate karta hoon. Koi solid Desi time management tips do!", "hinglish_habits"),
            QuickPrompt("💡", "Startup Idea", "India ke liye 3 high-potential modern micro-SaaS startup ideas brainstorm karo.", "hinglish_startup")
        )
        AssistantMode.ALL_ROUNDER -> listOf(
            QuickPrompt("🙏", "Who is Saddu AI?", "Namaste Saddu! Please introduce yourself, your core skills, and how we can work together.", "all_intro"),
            QuickPrompt("💻", "Kotlin Code Snippet", "Write a clean Kotlin singleton network repository with OkHttp and Moshi.", "all_code"),
            QuickPrompt("🧠", "Complex Problem", "Break down how Large Language Models actually process tokens and generate text.", "all_llm"),
            QuickPrompt("📝", "Daily Productivity", "Create an ultra-efficient 4-step morning routine to stay energized all day.", "all_prod")
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero Avatar & Greeting
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(CyanNeon.copy(alpha = 0.25f), Color(0xFF131722))
                    )
                )
                .border(2.dp, CyanNeon, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_saddu_logo),
                contentDescription = "Saddu AI Avatar",
                modifier = Modifier.size(62.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Namaste! Main hoon Saddu AI 🙏",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Your versatile AI companion for coding, creative ideas, problem-solving & daily life. Ask me anything!",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = TextSecondaryDark,
                lineHeight = 20.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 2x2 grid of quick prompt cards
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            prompts.chunked(2).forEach { rowPrompts ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowPrompts.forEach { promptItem ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceContainerDark)
                                .border(1.dp, SurfaceBorderDark, RoundedCornerShape(12.dp))
                                .clickable { onSelectPrompt(promptItem.prompt) }
                                .padding(12.dp)
                                .testTag("quick_prompt_${promptItem.tag}")
                        ) {
                            Column {
                                Text(
                                    text = promptItem.emoji,
                                    fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = promptItem.title,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = CyanNeon
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = promptItem.prompt,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextMutedDark,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    ),
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
