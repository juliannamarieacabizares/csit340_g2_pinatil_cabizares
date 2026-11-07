import { useEffect, useState } from 'react';
import { deleteItem, listItems } from '../authApi';
import ItemForm from '../components/ItemForm';
import Modal from '../components/Modal';
import './ItemsPage.css';

const PAGE_SIZE = 5;

export default function ItemsPage() {
  const [search, setSearch] = useState('');
  const [query, setQuery] = useState('');
  const [page, setPage] = useState(0);
  const [reloadToken, setReloadToken] = useState(0);
  const [result, setResult] = useState({ key: '', data: null, error: '' });
  const [modal, setModal] = useState(null); // { type: 'form' | 'delete', item }
  const [deleteError, setDeleteError] = useState('');
  const [isDeleting, setIsDeleting] = useState(false);
  const [notice, setNotice] = useState('');

  // Wait a little after typing, then search from the first page.
  useEffect(() => {
    const timer = setTimeout(() => {
      setQuery(search);
      setPage(0);
    }, 350);
    return () => clearTimeout(timer);
  }, [search]);

  // Load the table whenever the search, the page or the reload counter changes.
  const requestKey = `${query}|${page}|${reloadToken}`;
  useEffect(() => {
    let active = true;
    listItems({ search: query, page, size: PAGE_SIZE })
      .then((data) => {
        if (active) setResult({ key: requestKey, data, error: '' });
      })
      .catch((requestError) => {
        if (active) setResult({ key: requestKey, data: null, error: requestError.message });
      });
    return () => {
      active = false;
    };
  }, [requestKey, query, page]);

  // Hide the green message after 3 seconds.
  useEffect(() => {
    if (!notice) return undefined;
    const timer = setTimeout(() => setNotice(''), 3000);
    return () => clearTimeout(timer);
  }, [notice]);

  const isLoading = result.key !== requestKey;
  const items = result.data?.content ?? [];
  const totalPages = result.data?.totalPages ?? 0;
  const totalElements = result.data?.totalElements ?? 0;

  const reload = () => setReloadToken((token) => token + 1);
  const closeModal = () => {
    setModal(null);
    setDeleteError('');
  };

  const handleSaved = (message) => {
    closeModal();
    setNotice(message);
    reload();
  };

  const handleDelete = async () => {
    setDeleteError('');
    setIsDeleting(true);
    try {
      await deleteItem(modal.item.id);
      // If we removed the last row of this page, go back one page.
      if (items.length === 1 && page > 0) setPage(page - 1);
      closeModal();
      setNotice('Item deleted.');
      reload();
    } catch (requestError) {
      setDeleteError(requestError.message);
    } finally {
      setIsDeleting(false);
    }
  };

  return (
    <>
      <div className="crud-header">
        <h1>Items</h1>
        <button type="button" className="crud-btn crud-btn-primary" onClick={() => setModal({ type: 'form', item: null })}>
          + Add item
        </button>
      </div>

      <input
        className="crud-search"
        type="search"
        placeholder="Search by name or category"
        aria-label="Search items"
        value={search}
        onChange={(event) => setSearch(event.target.value)}
      />

      {notice && <p className="crud-notice" role="status">{notice}</p>}
      {result.error && <p className="crud-error" role="alert">{result.error}</p>}

      <div className="crud-table-wrap">
        <table className="crud-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Category</th>
              <th className="crud-number">Quantity</th>
              <th className="crud-number">Actions</th>
            </tr>
          </thead>
          <tbody>
            {isLoading && items.length === 0 && (
              <tr><td colSpan="4" className="crud-empty">Loading...</td></tr>
            )}
            {!isLoading && !result.error && items.length === 0 && (
              <tr><td colSpan="4" className="crud-empty">No items found.</td></tr>
            )}
            {items.map((item) => (
              <tr key={item.id}>
                <td>{item.name}</td>
                <td>{item.category}</td>
                <td className="crud-number">{item.quantity}</td>
                <td className="crud-number">
                  <button type="button" className="crud-btn" onClick={() => setModal({ type: 'form', item })}>Edit</button>{' '}
                  <button type="button" className="crud-btn crud-btn-danger" onClick={() => setModal({ type: 'delete', item })}>Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="crud-pagination">
        <span>{totalElements} item{totalElements === 1 ? '' : 's'}</span>
        <div>
          <button type="button" className="crud-btn" disabled={page === 0} onClick={() => setPage(page - 1)}>Previous</button>
          <span className="crud-page-label">Page {totalPages === 0 ? 0 : page + 1} of {totalPages}</span>
          <button type="button" className="crud-btn" disabled={page + 1 >= totalPages} onClick={() => setPage(page + 1)}>Next</button>
        </div>
      </div>

      {modal?.type === 'form' && (
        <Modal title={modal.item ? 'Edit item' : 'Add item'} onClose={closeModal}>
          <ItemForm item={modal.item} onSaved={handleSaved} onCancel={closeModal} />
        </Modal>
      )}

      {modal?.type === 'delete' && (
        <Modal title="Delete item" onClose={closeModal}>
          <p>Delete <strong>{modal.item.name}</strong>? This cannot be undone.</p>
          {deleteError && <p className="crud-error" role="alert">{deleteError}</p>}
          <div className="crud-actions">
            <button type="button" className="crud-btn" onClick={closeModal}>Cancel</button>
            <button type="button" className="crud-btn crud-btn-danger" disabled={isDeleting} onClick={handleDelete}>
              {isDeleting ? 'Deleting...' : 'Delete'}
            </button>
          </div>
        </Modal>
      )}
    </>
  );
}