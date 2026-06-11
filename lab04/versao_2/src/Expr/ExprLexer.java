package Expr;// Generated from C:/Users/brand/OneDrive/Documentos/GitHub/Compiladores/lab04/versao_2/src/Expr.g4 by ANTLR 4.13.2
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class ExprLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		PLUS=1, POW=2, MINUS=3, TIMES=4, DIV=5, LPAREN=6, RPAREN=7, ID=8, NUMBER=9, 
		WS=10;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"PLUS", "POW", "MINUS", "TIMES", "DIV", "LPAREN", "RPAREN", "ID", "NUMBER", 
			"WS", "DIGIT", "LETTER"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'+'", "'^'", "'-'", "'*'", "'/'", "'('", "')'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "PLUS", "POW", "MINUS", "TIMES", "DIV", "LPAREN", "RPAREN", "ID", 
			"NUMBER", "WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public ExprLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Expr.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\u0004\u0000\nX\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002\u0001"+
		"\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004"+
		"\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007"+
		"\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b"+
		"\u0007\u000b\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0002"+
		"\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0005"+
		"\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0003\u0007"+
		"*\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0005\u0007/\b\u0007\n\u0007"+
		"\f\u00072\t\u0007\u0001\b\u0004\b5\b\b\u000b\b\f\b6\u0001\b\u0005\b:\b"+
		"\b\n\b\f\b=\t\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0003\bD\b\b\u0001"+
		"\b\u0005\bG\b\b\n\b\f\bJ\t\b\u0003\bL\b\b\u0001\t\u0004\tO\b\t\u000b\t"+
		"\f\tP\u0001\t\u0001\t\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0000\u0000"+
		"\f\u0001\u0001\u0003\u0002\u0005\u0003\u0007\u0004\t\u0005\u000b\u0006"+
		"\r\u0007\u000f\b\u0011\t\u0013\n\u0015\u0000\u0017\u0000\u0001\u0000\u0003"+
		"\u0003\u0000\t\n\r\r  \u0001\u000009\u0002\u0000AZaz_\u0000\u0001\u0001"+
		"\u0000\u0000\u0000\u0000\u0003\u0001\u0000\u0000\u0000\u0000\u0005\u0001"+
		"\u0000\u0000\u0000\u0000\u0007\u0001\u0000\u0000\u0000\u0000\t\u0001\u0000"+
		"\u0000\u0000\u0000\u000b\u0001\u0000\u0000\u0000\u0000\r\u0001\u0000\u0000"+
		"\u0000\u0000\u000f\u0001\u0000\u0000\u0000\u0000\u0011\u0001\u0000\u0000"+
		"\u0000\u0000\u0013\u0001\u0000\u0000\u0000\u0001\u0019\u0001\u0000\u0000"+
		"\u0000\u0003\u001b\u0001\u0000\u0000\u0000\u0005\u001d\u0001\u0000\u0000"+
		"\u0000\u0007\u001f\u0001\u0000\u0000\u0000\t!\u0001\u0000\u0000\u0000"+
		"\u000b#\u0001\u0000\u0000\u0000\r%\u0001\u0000\u0000\u0000\u000f)\u0001"+
		"\u0000\u0000\u0000\u0011K\u0001\u0000\u0000\u0000\u0013N\u0001\u0000\u0000"+
		"\u0000\u0015T\u0001\u0000\u0000\u0000\u0017V\u0001\u0000\u0000\u0000\u0019"+
		"\u001a\u0005+\u0000\u0000\u001a\u0002\u0001\u0000\u0000\u0000\u001b\u001c"+
		"\u0005^\u0000\u0000\u001c\u0004\u0001\u0000\u0000\u0000\u001d\u001e\u0005"+
		"-\u0000\u0000\u001e\u0006\u0001\u0000\u0000\u0000\u001f \u0005*\u0000"+
		"\u0000 \b\u0001\u0000\u0000\u0000!\"\u0005/\u0000\u0000\"\n\u0001\u0000"+
		"\u0000\u0000#$\u0005(\u0000\u0000$\f\u0001\u0000\u0000\u0000%&\u0005)"+
		"\u0000\u0000&\u000e\u0001\u0000\u0000\u0000\'*\u0005_\u0000\u0000(*\u0003"+
		"\u0017\u000b\u0000)\'\u0001\u0000\u0000\u0000)(\u0001\u0000\u0000\u0000"+
		"*0\u0001\u0000\u0000\u0000+/\u0003\u0017\u000b\u0000,/\u0003\u0015\n\u0000"+
		"-/\u0005_\u0000\u0000.+\u0001\u0000\u0000\u0000.,\u0001\u0000\u0000\u0000"+
		".-\u0001\u0000\u0000\u0000/2\u0001\u0000\u0000\u00000.\u0001\u0000\u0000"+
		"\u000001\u0001\u0000\u0000\u00001\u0010\u0001\u0000\u0000\u000020\u0001"+
		"\u0000\u0000\u000035\u0003\u0015\n\u000043\u0001\u0000\u0000\u000056\u0001"+
		"\u0000\u0000\u000064\u0001\u0000\u0000\u000067\u0001\u0000\u0000\u0000"+
		"7L\u0001\u0000\u0000\u00008:\u0003\u0015\n\u000098\u0001\u0000\u0000\u0000"+
		":=\u0001\u0000\u0000\u0000;9\u0001\u0000\u0000\u0000;<\u0001\u0000\u0000"+
		"\u0000<C\u0001\u0000\u0000\u0000=;\u0001\u0000\u0000\u0000>?\u0005.\u0000"+
		"\u0000?D\u0003\u0015\n\u0000@A\u0003\u0015\n\u0000AB\u0005.\u0000\u0000"+
		"BD\u0001\u0000\u0000\u0000C>\u0001\u0000\u0000\u0000C@\u0001\u0000\u0000"+
		"\u0000DH\u0001\u0000\u0000\u0000EG\u0003\u0015\n\u0000FE\u0001\u0000\u0000"+
		"\u0000GJ\u0001\u0000\u0000\u0000HF\u0001\u0000\u0000\u0000HI\u0001\u0000"+
		"\u0000\u0000IL\u0001\u0000\u0000\u0000JH\u0001\u0000\u0000\u0000K4\u0001"+
		"\u0000\u0000\u0000K;\u0001\u0000\u0000\u0000L\u0012\u0001\u0000\u0000"+
		"\u0000MO\u0007\u0000\u0000\u0000NM\u0001\u0000\u0000\u0000OP\u0001\u0000"+
		"\u0000\u0000PN\u0001\u0000\u0000\u0000PQ\u0001\u0000\u0000\u0000QR\u0001"+
		"\u0000\u0000\u0000RS\u0006\t\u0000\u0000S\u0014\u0001\u0000\u0000\u0000"+
		"TU\u0007\u0001\u0000\u0000U\u0016\u0001\u0000\u0000\u0000VW\u0007\u0002"+
		"\u0000\u0000W\u0018\u0001\u0000\u0000\u0000\n\u0000).06;CHKP\u0001\u0006"+
		"\u0000\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}