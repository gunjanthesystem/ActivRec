package org.activity.jmotif.sax.datastructures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * The SAX data structure. Implements optimized storage for the SAX data.
 * 
 * @author Pavel Senin.
 * 
 */
public class SAXFrequencyData implements Iterable<SAXFrequencyEntry> {

  private final HashMap<String, SAXFrequencyEntry> data;

  private HashMap<Integer, String> positionsAndWords;

  private ArrayList<Integer> allIndices;

  /**
   * Constructor.
   */
  public SAXFrequencyData() {
    super();
    this.data = new HashMap<String, SAXFrequencyEntry>();
  }

  /**
   * Put the substring with it's index into the storage.
   * 
   * @param substring The substring value.
   * @param idx The substring entry index.
   */
  public void put(String substring, int idx) {
    SAXFrequencyEntry sfe = this.data.get(substring);
    if (null == sfe) {
      this.data.put(substring, new SAXFrequencyEntry(substring, idx));
    }
    else {
      sfe.put(idx);
    }
  }

  /**
   * Get the internal hash size.
   * 
   * @return The number of substrings in the data structure.
   */
  public Integer size() {
    return this.data.size();
  }

  /**
   * Check if the data includes substring.
   * 
   * @param substring The query substring.
   * @return TRUE is contains, FALSE if not.
   */
  public boolean contains(String substring) {
    return this.data.containsKey(substring);
  }

  /**
   * Get the entry information.
   * 
   * @param substring The key get entry for.
   * @return The entry containing the substring occurence frequency information.
   */
  public SAXFrequencyEntry get(String substring) {
    return this.data.get(substring);
  }

  /**
   * Get the set of sorted by the occurence frequencies.
   * 
   * @return The set of sorted by the occurence frequencies.
   */
  public List<SAXFrequencyEntry> getSortedFrequencies() {
    List<SAXFrequencyEntry> l = new ArrayList<SAXFrequencyEntry>();
    l.addAll(this.data.values());
    Collections.sort(l);
    return l;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterator<SAXFrequencyEntry> iterator() {
    return this.data.values().iterator();
  }

  /**
   * Get all SAX subsequences as one string separated by a specified string.
   * 
   * @param separator The separator.
   * @return SAX all SAX words as a string.
   */
  public String getSAXString(String separator) {

    // hash mapping, position -> word
    //
    this.positionsAndWords = new HashMap<Integer, String>();

    // all timeseries indexes where word is mapped to
    //
    allIndices = new ArrayList<Integer>();

    // iterate over all the frequency entries filling up above data structures
    //
    Iterator<SAXFrequencyEntry> freqIterator = iterator();
    while (freqIterator.hasNext()) {

      SAXFrequencyEntry freqEntry = freqIterator.next();
      ArrayList<Integer> entryOccurrences = freqEntry.getEntries();

      // save words
      for (int index : entryOccurrences) {
        positionsAndWords.put(index, freqEntry.getSubstring());
      }

      // save indexes
      this.allIndices.addAll(entryOccurrences);

    }

    // sort by the position
    //
    Collections.sort(allIndices, new Comparator<Integer>() {
      public int compare(Integer int1, Integer int2) {
        return Integer.valueOf(int1).compareTo(Integer.valueOf(int2));
      }
    });

    // make a string
    //
    StringBuilder sb = new StringBuilder();
    for (int index : allIndices) {
      sb.append(positionsAndWords.get(index));
      sb.append(separator);
    }
    return sb.toString();
  }

  public HashMap<Integer, String> getPositionsAndWords() {
    return positionsAndWords;
  }

  public ArrayList<Integer> getAllIndices() {
    return allIndices;
  }

}
