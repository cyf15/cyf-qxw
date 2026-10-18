package lab.pvp.persistence;

import lab.pvp.model.Hero;

public record MatchRecord(long id, String playedAt, Hero player1, Hero player2, int winner, int turns) {}
