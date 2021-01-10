package com.threecrickets.jygments.grammar;

import java.util.StringJoiner;

public class PythonStyleConverter {
	public static void main(String[] args) {
		String style =
				"        Text:                      \"#f8f8f2\", # class:  ''\n" +
				"        Whitespace:                \"\",        # class: 'w'\n" +
				"        Error:                     \"#960050 bg:#1e0010\", # class: 'err'\n" +
				"        Other:                     \"\",        # class 'x'\n" +
				"\n" +
				"        Comment:                   \"#75715e\", # class: 'c'\n" +
				"        Comment.Multiline:         \"\",        # class: 'cm'\n" +
				"        Comment.Preproc:           \"\",        # class: 'cp'\n" +
				"        Comment.Single:            \"\",        # class: 'c1'\n" +
				"        Comment.Special:           \"\",        # class: 'cs'\n" +
				"\n" +
				"        Keyword:                   \"#66d9ef\", # class: 'k'\n" +
				"        Keyword.Constant:          \"\",        # class: 'kc'\n" +
				"        Keyword.Declaration:       \"\",        # class: 'kd'\n" +
				"        Keyword.Namespace:         \"#f92672\", # class: 'kn'\n" +
				"        Keyword.Pseudo:            \"\",        # class: 'kp'\n" +
				"        Keyword.Reserved:          \"\",        # class: 'kr'\n" +
				"        Keyword.Type:              \"\",        # class: 'kt'\n" +
				"\n" +
				"        Operator:                  \"#f92672\", # class: 'o'\n" +
				"        Operator.Word:             \"\",        # class: 'ow' - like keywords\n" +
				"\n" +
				"        Punctuation:               \"#f8f8f2\", # class: 'p'\n" +
				"\n" +
				"        Name:                      \"#f8f8f2\", # class: 'n'\n" +
				"        Name.Attribute:            \"#a6e22e\", # class: 'na' - to be revised\n" +
				"        Name.Builtin:              \"\",        # class: 'nb'\n" +
				"        Name.Builtin.Pseudo:       \"\",        # class: 'bp'\n" +
				"        Name.Class:                \"#a6e22e\", # class: 'nc' - to be revised\n" +
				"        Name.Constant:             \"#66d9ef\", # class: 'no' - to be revised\n" +
				"        Name.Decorator:            \"#a6e22e\", # class: 'nd' - to be revised\n" +
				"        Name.Entity:               \"\",        # class: 'ni'\n" +
				"        Name.Exception:            \"#a6e22e\", # class: 'ne'\n" +
				"        Name.Function:             \"#a6e22e\", # class: 'nf'\n" +
				"        Name.Property:             \"\",        # class: 'py'\n" +
				"        Name.Label:                \"\",        # class: 'nl'\n" +
				"        Name.Namespace:            \"\",        # class: 'nn' - to be revised\n" +
				"        Name.Other:                \"#a6e22e\", # class: 'nx'\n" +
				"        Name.Tag:                  \"#f92672\", # class: 'nt' - like a keyword\n" +
				"        Name.Variable:             \"\",        # class: 'nv' - to be revised\n" +
				"        Name.Variable.Class:       \"\",        # class: 'vc' - to be revised\n" +
				"        Name.Variable.Global:      \"\",        # class: 'vg' - to be revised\n" +
				"        Name.Variable.Instance:    \"\",        # class: 'vi' - to be revised\n" +
				"\n" +
				"        Number:                    \"#ae81ff\", # class: 'm'\n" +
				"        Number.Float:              \"\",        # class: 'mf'\n" +
				"        Number.Hex:                \"\",        # class: 'mh'\n" +
				"        Number.Integer:            \"\",        # class: 'mi'\n" +
				"        Number.Integer.Long:       \"\",        # class: 'il'\n" +
				"        Number.Oct:                \"\",        # class: 'mo'\n" +
				"\n" +
				"        Literal:                   \"#ae81ff\", # class: 'l'\n" +
				"        Literal.Date:              \"#e6db74\", # class: 'ld'\n" +
				"\n" +
				"        String:                    \"#e6db74\", # class: 's'\n" +
				"        String.Backtick:           \"\",        # class: 'sb'\n" +
				"        String.Char:               \"\",        # class: 'sc'\n" +
				"        String.Doc:                \"\",        # class: 'sd' - like a comment\n" +
				"        String.Double:             \"\",        # class: 's2'\n" +
				"        String.Escape:             \"#ae81ff\", # class: 'se'\n" +
				"        String.Heredoc:            \"\",        # class: 'sh'\n" +
				"        String.Interpol:           \"\",        # class: 'si'\n" +
				"        String.Other:              \"\",        # class: 'sx'\n" +
				"        String.Regex:              \"\",        # class: 'sr'\n" +
				"        String.Single:             \"\",        # class: 's1'\n" +
				"        String.Symbol:             \"\",        # class: 'ss'\n" +
				"\n" +
				"\n" +
				"        Generic:                   \"\",        # class: 'g'\n" +
				"        Generic.Deleted:           \"#f92672\", # class: 'gd',\n" +
				"        Generic.Emph:              \"italic\",  # class: 'ge'\n" +
				"        Generic.Error:             \"\",        # class: 'gr'\n" +
				"        Generic.Heading:           \"\",        # class: 'gh'\n" +
				"        Generic.Inserted:          \"#a6e22e\", # class: 'gi'\n" +
				"        Generic.Output:            \"#66d9ef\", # class: 'go'\n" +
				"        Generic.Prompt:            \"bold #f92672\", # class: 'gp'\n" +
				"        Generic.Strong:            \"bold\",    # class: 'gs'\n" +
				"        Generic.Subheading:        \"#75715e\", # class: 'gu'\n" +
				"        Generic.Traceback:         \"\",        # class: 'gt'";

		for (String line : style.split("\n")) {
			final String[] keyValue = line.trim().split(",");
			if (keyValue.length < 2) {
				continue;
			}

			final int index = keyValue[0].indexOf(':');
			String name = keyValue[0].substring(0, index).trim();
			String values = keyValue[0].substring(index + 1).trim();

			if (values.contains(" ")) {
				final StringJoiner joiner = new StringJoiner(",");
				for (String s : values.substring(1, values.length() - 1).split(" ")){
					joiner.add("\"" + s + "\"");
				}
				values = joiner.toString();
			}

			System.out.println(String.format("add( \"%s\", %s);", name, values));
		}
	}

	private static String[] omitEmpty(String[] values) {
		String[] kv = new String[2];
		int i = 0;
		for (String value : values) {
			if (value.isEmpty()) {
				continue;
			}
			kv[i] = value.trim();
			i++;
			if (i == 2) {
				break;
			}
		}
		return kv;
	}
}
