import { useState } from 'react';
import { createItem, updateItem } from '../authApi';

// Used for both "Add item" (item is null) and "Edit item" (item is the row to edit).
export default function ItemForm({ item, onSaved, onCancel }) {
  const [formData, setFormData] = useState({
    name: item?.name ?? '',
    category: item?.category ?? '',
    quantity: item?.quantity ?? 0,
  });
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleChange = (event) => {
    setFormData({ ...formData, [event.target.name]: event.target.value });
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');
    setIsSubmitting(true);

    const payload = {
      name: formData.name.trim(),
      category: formData.category.trim(),
      quantity: Number(formData.quantity),
    };

    try {
      if (item) {
        await updateItem(item.id, payload);
      } else {
        await createItem(payload);
      }
      onSaved(item ? 'Item updated.' : 'Item added.');
    } catch (requestError) {
      setError(requestError.message);
      setIsSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      {error && <p className="crud-error" role="alert">{error}</p>}
      <div className="crud-field">
        <label htmlFor="item-name">Name</label>
        <input id="item-name" name="name" maxLength={100} value={formData.name} onChange={handleChange} required />
      </div>
      <div className="crud-field">
        <label htmlFor="item-category">Category</label>
        <input id="item-category" name="category" maxLength={100} value={formData.category} onChange={handleChange} />
      </div>
      <div className="crud-field">
        <label htmlFor="item-quantity">Quantity</label>
        <input id="item-quantity" name="quantity" type="number" min="0" value={formData.quantity} onChange={handleChange} required />
      </div>
      <div className="crud-actions">
        <button type="button" className="crud-btn" onClick={onCancel}>Cancel</button>
        <button type="submit" className="crud-btn crud-btn-primary" disabled={isSubmitting}>
          {isSubmitting ? 'Saving...' : 'Save'}
        </button>
      </div>
    </form>
  );
}