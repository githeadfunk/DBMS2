/*
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.*;
public class Lexer {
	public String statement;
	public Lexer(String s){
		this.statement = s;
	}
	
  	public ParseTree gettree() {
	  
   // String raw_statement = "SELECT * FROM course WHERE grade = \"C\" AND [ exam > 70 OR project > 70 ] AND NOT ( exam * 30 + homework * 20 + project * 50 ) / 100 < 60";
	  //String raw_statement = "INSERT INTO course (sid, homework, project, exam, grade) VALUES (\"2 d d\", 0, 100, 100, \"E  f\")";
	  //String raw_statement = "wyh \"12 3\" \"1234\"";
	  //String raw_statement = "CREATE TABLE course (sid INT, homework INT, project INT, exam INT, grade STR20)";
	  //String raw_statement = "SELECT wyh,atm FROM course, course2 WHERE course.sid = course2.sid AND course.exam > course2.exam;";
	  
	  String raw_statement = this.statement;
	  ArrayList<String> statement = new ArrayList<String>();
	  int count = raw_statement.length() - raw_statement.replaceAll("\"", "").length();
	  if(count > 0){//IN the case of "" in statement
		  String regex1 = "[\"\"]";
		  String regex2 = "[ ,()]";
		  for(int i = 0; i < raw_statement.split(regex1).length; i++){
		      //System.out.println(raw_statement.split(regex1)[i]);
		      if(i % 2 == 0){
		    	  String[] after_split = raw_statement.split(regex1)[i].split(regex2);
		    	  for(int j = 0; j < after_split.length; j++){
		    		  if(!after_split[j].isEmpty()){
				    	  statement.add(after_split[j].toLowerCase());
				      }
		    	  }
		      }
		      else{
		    	  statement.add(raw_statement.split(regex1)[i].toLowerCase());
		      }
				  
		  }
	  }
	  else{// IN the case of no "" in statement
		  String regex = "[ ,()]";
		  for(int i = 0; i < raw_statement.split(regex).length; i++){
		      if(!raw_statement.split(regex)[i].isEmpty()){
		    	  statement.add(raw_statement.split(regex)[i].toLowerCase());
		      }
		  }
	  }
	  
	  
	  PTConstruct con = new PTConstruct(statement);
	  ParseTree t;
	  t = con.construct();
	  return t;
  }
}
*/

import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Lexer {
	public String statement;
	public Lexer(String s){
		this.statement = s;
	}

	public static enum TokenType {
		// Token types cannot have underscores
		NUMBER("-?[0-9]+"), QUOTE("\\\"([a-zA-Z0-9 \']+)\\\""), WHITESPACE("[ \t\f\r\n]+"), ATTRIBUTENAME("[a-zA-Z][0-9a-zA-Z]*\\.[a-zA-Z][0-9a-zA-Z]*"), NAME("[a-zA-Z][A-Z0-9a-z]*"), BINARYOP("[*|/|+|-|,|.|;|(|)|[|]|<|>|=|\"]");

		public final String pattern;

		private TokenType(String pattern) {
			this.pattern = pattern;
		}
	}

	public static class Token {
		public TokenType type;
		public String data;
		public Token(TokenType type, String data) {
			this.type = type;
			this.data = data;
		}

    @Override
    	public String toString() {
    		return String.format("(%s %s)", type.name(), data);
    	}
	}

	public static ArrayList<Token> lex(String input) {
		// The tokens to return
		ArrayList<Token> tokens = new ArrayList<Token>();

		// Lexer logic begins here
		StringBuffer tokenPatternsBuffer = new StringBuffer();
		for (TokenType tokenType : TokenType.values())
			tokenPatternsBuffer.append(String.format("|(?<%s>%s)", tokenType.name(), tokenType.pattern));
		Pattern tokenPatterns = Pattern.compile(new String(tokenPatternsBuffer.substring(1)));

		// Begin matching tokens
		Matcher matcher = tokenPatterns.matcher(input);
		while (matcher.find()) {
			if (matcher.group(TokenType.NUMBER.name()) != null) {
				tokens.add(new Token(TokenType.NUMBER, matcher.group(TokenType.NUMBER.name())));
				continue;
			} 
			else if (matcher.group(TokenType.QUOTE.name()) != null) {
				tokens.add(new Token(TokenType.QUOTE, matcher.group(TokenType.QUOTE.name())));
				continue;
			}
			else if (matcher.group(TokenType.ATTRIBUTENAME.name()) != null) {
				tokens.add(new Token(TokenType.ATTRIBUTENAME, matcher.group(TokenType.ATTRIBUTENAME.name())));
				continue;
			}
			else if (matcher.group(TokenType.NAME.name()) != null) {
				tokens.add(new Token(TokenType.NAME, matcher.group(TokenType.NAME.name())));
				continue;
			}
			//else if (matcher.group(TokenType.RESERVED.name()) != null) {
			//	tokens.add(new Token(TokenType.RESERVED, matcher.group(TokenType.RESERVED.name())));
			//	continue;
			//} 
			else if (matcher.group(TokenType.BINARYOP.name()) != null) {
				tokens.add(new Token(TokenType.BINARYOP, matcher.group(TokenType.BINARYOP.name())));
				continue;
			}      
			else if (matcher.group(TokenType.WHITESPACE.name()) != null)
				continue;
		}

		return tokens;
	}
	
	public ParseTree gettree(){
		//String select_statement = "SELECT * FROM course WHERE grade = \"C D\" AND ( exam > 70 OR project > 70 ) AND NOT ( exam * 30 + homework * 20 + project * 50 ) / 100 < 60";
		//String select_statement = "SELECT (zy, ww) FROM course, course2 ;";
		//String select_statement = "SELECT wyh,atm FROM c, course2 WHERE course.sid = course2.sid AND course.exam > course2.exam;";
		// Create tokens and print them
		//ArrayList<Token> tokens = lex(input);
		
		ArrayList<Token> tokens = lex(statement);
		ArrayList<String> mylist = new ArrayList<String>();
		for (Token token : tokens){
			mylist.add(token.data);
			//System.out.println(token);
		}
		System.out.println(mylist.size());
		PTConstruct con = new PTConstruct(mylist);
		ParseTree t;
		t = con.construct();
		return t;
		//ETConstruct et = new ETConstruct(t);
		//ExpressionTree e;
		//e = et.construct();
		//System.out.println("stop2");

	}
}




