package dev.falsegamemaster.propengine.util;

import java.util.Set;

public interface TagExpression {

    boolean test(Set<String> tags);

    class Parser {

        public static TagExpression parse(String input) {
            Parser parser = new Parser(input);
            TagExpression expression = parser.parseOr();
            parser.skipWhitespace();
            if (!parser.isAtEnd()) throw new IllegalArgumentException("Unexpected token at position " + parser.pos);
            return expression;
        }

        private final String input;
        private int pos = 0;

        private Parser(String input) {
            this.input = input;
        }

        private TagExpression parseOr() {
            TagExpression left = parseXor();
            while (match('|')) {
                TagExpression right = parseXor();
                TagExpression previous = left;
                left = tags -> previous.test(tags) || right.test(tags);
            }
            return left;
        }

        private TagExpression parseXor() {
            TagExpression left = parseAnd();
            while (match('^')) {
                TagExpression right = parseAnd();
                TagExpression previous = left;
                left = tags -> previous.test(tags) ^ right.test(tags);
            }
            return left;
        }

        private TagExpression parseAnd() {
            TagExpression left = parseUnary();
            while (match('&')) {
                TagExpression right = parseUnary();
                TagExpression previous = left;
                left = tags -> previous.test(tags) && right.test(tags);
            }
            return left;
        }

        private TagExpression parseUnary() {
            if (match('!')) {
                TagExpression inner = parseUnary();
                return tags -> !inner.test(tags);
            }
            return parsePrimary();
        }

        private TagExpression parsePrimary() {
            skipWhitespace();
            if (match('(')) {
                TagExpression expression = parseOr();
                if (!match(')')) throw new IllegalArgumentException("Missing ')' at position " + pos);
                return expression;
            }
            return parseTag();
        }

        private TagExpression parseTag() {
            skipWhitespace();
            int start = pos;
            while (!isAtEnd()) {
                char c = input.charAt(pos);
                if (Character.isLetterOrDigit(c) || c == '-' || c == '+' || c == '.' || c == '_') pos ++;
                else break;
            }
            if (start == pos) throw new IllegalArgumentException("Expected tag at position " + pos);
            String tag = input.substring(start, pos);
            return tags -> tags.contains(tag);
        }

        private boolean match(char expected) {
            skipWhitespace();
            if (isAtEnd() || input.charAt(pos) != expected) return false;
            pos ++;
            return true;
        }

        private void skipWhitespace() {
            while (!isAtEnd() && Character.isWhitespace(input.charAt(pos))) {
                pos ++;
            }
        }

        private boolean isAtEnd() {
            return pos >= input.length();
        }

    }

}
