
function normalizeSearchQuery(raw) {
    if (!raw) return '';
    let q = raw.trim();
    q = q.replace(/\s+/g, ' ');      // รวมช่องว่างซ้ำ ๆ
    q = q.replace(/(\.)\s+/g, '$1'); // แก้ "คพ. 101" → "คพ.101"
    
    if (/^คพ\./i.test(q)) {
        q = q.replace(/^คพ\./i, 'CS');
    } else if (/^คพ/i.test(q)) {
        q = q.replace(/^คพ/i, 'CS');
    }
    return q;
}


describe('Frontend Logic: Search Normalization', () => {

    test('TC-SEARCH-NORM-01: Should handle null or empty string', () => {
        expect(normalizeSearchQuery(null)).toBe('');
        expect(normalizeSearchQuery('')).toBe('');
    });

    test('TC-SEARCH-NORM-02: Should convert Thai prefix (คพ.) to English (CS)', () => {
        expect(normalizeSearchQuery('คพ.101')).toBe('CS101');
        expect(normalizeSearchQuery('คพ261')).toBe('CS261');
    });

    test('TC-SEARCH-NORM-03: Should fix spaces in Thai prefix', () => {
        expect(normalizeSearchQuery('คพ. 101')).toBe('CS101'); 
    });

    // จับ Defect ในเคสนี้
    test('TC-SEARCH-NORM-04: Should remove spaces in English course codes', () => {
        // ถ้าพิมพ์ "CS 261" มันควรจะถูกแปลงเป็น "CS261" เพื่อให้ DB หาเจอ
        expect(normalizeSearchQuery('CS 261')).toBe('CS261'); 
    });

});