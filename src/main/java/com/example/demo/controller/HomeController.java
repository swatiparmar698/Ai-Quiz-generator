package com.example.demo.controller;

import java.util.List;
import java.util.Scanner;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.model.Question;

import jakarta.servlet.http.HttpSession;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Controller
public class HomeController {
private final ChatClient chatClient;

public HomeController(ChatClient.Builder chatClient) {

	this.chatClient = chatClient.build();
}

@GetMapping
public String show(HttpSession session ,ModelMap model)
{

String s1= """
		Output ONLY a valid raw JSON array. Do not include any conversational text, Markdown formatting, intro or outro.

Generate 10 random Java True/False declarative statements.

CRITICAL: Do NOT generate questions starting with "what", "why", "how", "which", or "Is". Every item must be a fact-based statement.

Each JSON object must have exactly these three keys:
1. "qno" (integer; sequential number 1 to 10)
2. "question" (the statement string)
3. "ans" (boolean: true/false)

Strict Format Example:

[
  {
    "qno": 1,
    "question": "An interface in Java can have private methods since Java 9.",
    "ans":true/false
  }
]
return Always complete JSON array And end the response  with ]
""";
String s2=chatClient.prompt(s1).call().content();
/*
s2=s2.trim();
if(!s2.endsWith("}")&&!s2.endsWith("]"))
{
	s2=s2+"}";
	}
if(!s2.endsWith("]"))
{
	s2=s2+"]";
	}


	String s2= """
			[
  {
    "qno": 1,
    "question": "A class can extend multiple interfaces in Java.",
    "ans": false
  },
  {
    "qno": 2,
    "question": "In Java, a public static variable is stateless.",
    "ans": true
  },
  {
    "qno": 3,
    "question": "A final method cannot be overridden in Java.",
    "ans": false
  },
  {
    "qno": 4,
    "question": "Java's primitive data types are passed by value, not reference.",
    "ans": true
  },
  {
    "qno": 5,
    "question": "A static method in Java can access instance variables without parameters.",
    "ans": false
  }
]
			""";
			*/
ObjectMapper mapper=new ObjectMapper();
List<Question> list=mapper.readValue(s2, new TypeReference<List<Question>>(){});
session.setAttribute("list", list);
session.setAttribute("count", 0);
model.addAttribute("current_question", list.get(0));
return "question";
}
@GetMapping("/next")
public String nextQuestion(HttpSession session,ModelMap model,@RequestParam("userAns") boolean userAns)
{
	List<Question> list=(List<Question>)session.getAttribute("list");
	int count=(int)session.getAttribute("count");
	list.get(count).setUserAns(userAns);
	if(userAns==list.get(count).isAns())
	{
		list.get(count).setMarks(1);
	}
	if(count==9)
	{
		int c=0;
		for(Question q:list)
		{
			c=c+q.getMarks();
		}
		model.addAttribute("total",c);
		
		return "result";
	}
	
	session.setAttribute("count",++count);
	//System.out.println("userAns => " + userAns + "\tcorrect ans => " + list.get(count).isAns());
	
	model.addAttribute("current_question",list.get(count));
	return "question";
	
}
}
