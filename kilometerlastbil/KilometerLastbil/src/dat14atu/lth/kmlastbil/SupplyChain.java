package dat14atu.lth.kmlastbil;

import java.util.ArrayList;
import java.util.List;

public class SupplyChain {
	
	private boolean endOfLine = false;
	private boolean hasBeenPulled = false;
	private int effectivePull = 0;
	
	private Model model;
	private List<Link> chain;
	private Link lastLink;
	private boolean containsPull[][];
	private boolean containsPush[][];
	
	protected Link getLastLink() {
		return lastLink;
	}
	protected boolean containsPull(int factory, int store) {
		return containsPull[factory][store];
	}
	protected boolean containsPush(int factory, int store) {
		return containsPush[factory][store];
	}
	
	protected SupplyChain(Model model, Link baseLink){
		this.model = model;
		chain = new ArrayList<Link>();
		containsPull = new boolean[model.getFactories()][model.getStores()];
		containsPush = new boolean[model.getFactories()][model.getStores()];
		addLinkForce(baseLink);
	}
	private SupplyChain(Model model) {
		this.model = model;
		chain = new ArrayList<Link>();
		containsPull = new boolean[model.getFactories()][model.getStores()];
		containsPush = new boolean[model.getFactories()][model.getStores()];
	}
	
	/**
	 * 
	 * @param link
	 * @return True = end of line baby.
	 * @throws Exception Don't fuck up.
	 */
	protected boolean addLink(Link link) throws Exception {
		
		if(containsPull(link.getFactory(), link.getStoreFrom())) {
			throw(new Exception("Error|SupplyChain.addLink:DuplicatePull| - Cannot add a link with an already existing pull."));
		}
		if(containsPush(link.getFactory(), link.getStoreTo())) {
			throw(new Exception("Error|SupplyChain.addLink:DuplicatePush| - Cannot add a link with an already existing push."));
		}
		if(containsPull(link.getFactory(), link.getStoreTo())) {
			System.out.println("CHAIN: " + toString());
			System.out.println("LINK: " + link.toString());
			throw(new Exception("Error|SupplyChain.addLink:IncompatiblePull| - Cannot add pull working against an already existing push."));
		}
		if(containsPush(link.getFactory(), link.getStoreFrom())) {
			throw(new Exception("Error|SupplyChain.addLink:IncompatiblePush| - Cannot add push working against an already existing pull."));
		}
		if(endOfLine) {
			System.out.println("Warning|SupplyChain.addLink:EndOfLine| - Cannot add more links after end of line is reached.");
			return true;
		}
		
		return addLinkForce(link);
	}
	
	// private addLink, disregard error checking and such because we don't need that in here
	private boolean addLinkForce(Link link) {
		chain.add(link);
		endOfLine = link.getStoreFrom() == chain.get(0).getStoreTo();
		lastLink = link;
		containsPull[link.getFactory()][link.getStoreFrom()] = true;
		containsPush[link.getFactory()][link.getStoreTo()] = true;
		return endOfLine;
	}
	
	protected int getTotalDistanceGain() {
		int distanceGain = 0;
		for(Link link : chain) {
			distanceGain += link.getDistanceGain();
		}
		return distanceGain;
	}
	
	protected int invokePull(int desiredSupplyFlow) {
		if(!endOfLine) {
			System.out.println("Warning|SupplyChain.invokePull:ChainNotClosed| - Attempting to pull before end of line.");
			return 0;
		}
		
		for(Link link : chain) {
			desiredSupplyFlow = Math.min(desiredSupplyFlow, link.getMaxSupplyFlow());
		}
		int effectivePull = desiredSupplyFlow;
		for(Link link : chain) {
			effectivePull = Math.min(effectivePull, link.invokePull(desiredSupplyFlow));
		}
		this.effectivePull = effectivePull;
		hasBeenPulled = true;
		return effectivePull;
	}
	
	protected int invokePush(int desiredSupplyFlow) {
		if(!hasBeenPulled) {
			System.out.println("Warning|SupplyChain.invokePush:NoPull| - Attempting to push before a pull has been made.");
			return 0;
		}
		
		desiredSupplyFlow = Math.min(desiredSupplyFlow, effectivePull);
		double averagePush = 0;
		for(Link link : chain) {
			averagePush += link.invokePush(desiredSupplyFlow);
		}
		averagePush /= chain.size();
		return (int) averagePush;
	}
	
	protected SupplyChain clone() {
		SupplyChain clone = new SupplyChain(model);
		for(Link link : chain) {
			if(clone.addLinkForce(link.clone())) {
				System.out.println("Warning|SupplyChain.clone:WhatTheFuck| - Not supposed to happen, ever.");
				break;
			}
		}
		return clone;
	}
	
	@Override
	public String toString() {
		String s = "";
		for(Link link : chain) {
			s += link.toString();
		}
		return s;
	}
}
