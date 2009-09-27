/*
 * 作成日: 2009/05/19
 */
package jp.ac.fit.asura.nao.strategy;

/**
 *
 * ゲームの状態を表現します.
 *
 * RoboCup SPLルールブック中のGame stateに対応しています.
 *
 * ペナライズなどの状態はここでは表現していないので注意.
 *
 * @author sey
 *
 * @version $Id: $
 *
 */
public enum GameState {
	INITIAL, READY, SET, PLAYING, FINISHED
}
