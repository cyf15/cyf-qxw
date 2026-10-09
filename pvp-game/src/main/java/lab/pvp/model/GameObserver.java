package lab.pvp.model;

@FunctionalInterface
public interface GameObserver { void onChanged(BattleSnapshot snapshot); }
