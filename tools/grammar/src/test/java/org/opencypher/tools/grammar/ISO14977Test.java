package org.opencypher.tools.grammar;

import java.io.StringWriter;

import org.junit.Rule;
import org.junit.Test;
import org.opencypher.grammar.Fixture;
import org.opencypher.grammar.Grammar;

import static org.junit.Assert.assertEquals;
import static org.opencypher.grammar.Grammar.atLeast;
import static org.opencypher.grammar.Grammar.literal;
import static org.opencypher.grammar.Grammar.nonTerminal;
import static org.opencypher.grammar.Grammar.oneOf;
import static org.opencypher.grammar.Grammar.oneOrMore;
import static org.opencypher.grammar.Grammar.optional;
import static org.opencypher.grammar.Grammar.repeat;
import static org.opencypher.grammar.Grammar.sequence;
import static org.opencypher.grammar.Grammar.zeroOrMore;
import static org.opencypher.tools.output.Output.lines;

public class ISO14977Test
{
    public final @Rule Fixture fixture = new Fixture();

    @Test
    public void shouldRenderLiteral() throws Exception
    {
        verify( production( "foo", literal( "FOO" ) ),
                "foo = \"FOO\" ;" );
    }

    @Test
    public void shouldRenderAlternativesOfProduction() throws Exception
    {
        verify( production( "one", literal( "A" ), literal( "B" ) ),
                "one = \"A\"",
                "    | \"B\"",
                "    ;" );
    }

    @Test
    public void shouldRenderSequenceWithAlternatives() throws Exception
    {
        verify( production( "something", sequence(
                literal( "A" ),
                oneOf( literal( "B" ), literal( "C" ) ),
                literal( "D" ) ) ),
                "something = \"A\", (\"B\" | \"C\"), \"D\" ;" );
    }

    @Test
    public void shouldRenderAlternativeSequences() throws Exception
    {
        verify( production( "alts", sequence( literal( "A" ), literal( "B" ) ),
                            sequence( literal( "C" ), literal( "D" ) ) ),
                "alts = (\"A\", \"B\")",
                "     | (\"C\", \"D\")",
                "     ;" );
    }

    @Test
    public void shouldRenderRepetitionWithExactCount() throws Exception
    {
        verify( production( "repeat", repeat( 6, literal( "hello" ) ) ),
                "repeat = 6 * \"hello\" ;" );
    }

    @Test
    public void shouldRenderRepetitionWithMinAndMax() throws Exception
    {
        verify( production( "repeat", repeat( 5, 10, literal( "hello" ) ) ),
                "repeat = 5 * \"hello\", 5 * [\"hello\"] ;" );
    }

    @Test
    public void shouldRenderRepetitionWithMin() throws Exception
    {
        verify( production( "repeat", atLeast( 3, literal( "hello" ) ) ),
                "repeat = 3 * \"hello\", {\"hello\"} ;" );
    }

    @Test
    public void shouldRenderRepetitionWithMax() throws Exception
    {
        verify( production( "repeat", repeat( 0, 7, literal( "hello" ) ) ),
                "repeat = 7 * [\"hello\"] ;" );
    }

    @Test
    public void shouldRenderOneOrMore() throws Exception
    {
        verify( production( "repeat", oneOrMore( literal( "hello" ) ) ),
                "repeat = \"hello\", {\"hello\"} ;" );
    }

    @Test
    public void shouldRenderZeroOrMore() throws Exception
    {
        verify( production( "repeat", zeroOrMore( literal( "hello" ) ) ),
                "repeat = {\"hello\"} ;" );
    }

    @Test
    public void shouldRenderAlternativeRepetitions() throws Exception
    {
        verify( production( "stuff", zeroOrMore( literal( "foo"), literal("bar") ),
                            repeat( 5, literal( "abc" ), literal( "xyz" ) )),
                "stuff = {\"foo\", \"bar\"}",
                "      | 5 * (\"abc\", \"xyz\")",
                "      ;");
    }

    @Test
    public void shouldRenderOptional() throws Exception
    {
        verify( production( "opt", optional( literal( "foo" ) ) ),
                "opt = [\"foo\"] ;" );
    }

    @Test
    public void shouldRenderRecursiveDefinition() throws Exception
    {
        verify( production( "rec", sequence( literal( "A" ), optional( nonTerminal( "rec" ) ), literal( "B" ) ) ),
                "rec = \"A\", [rec], \"B\" ;" );
    }

    Grammar.Builder production( String name, Grammar.Term first, Grammar.Term... alternatives )
    {
        return fixture.grammar().production( name, first, alternatives );
    }

    static void verify( Grammar.Builder grammar, String... lines )
    {
        StringWriter writer = new StringWriter();
        ISO14977.write( grammar.build(), writer );
        assertEquals( lines( lines ).trim(), writer.toString().trim() );
    }
}
