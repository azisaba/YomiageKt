package net.azisaba.yomiagekt.extension

import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction

fun ReplyCallbackAction.respond(content: String) = setContent(content).queue()
