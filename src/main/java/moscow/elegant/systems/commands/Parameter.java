package moscow.elegant.systems.commands;

public record Parameter<T>(String name, boolean required, boolean vararg, ParameterValidator<T> validator) {
}
