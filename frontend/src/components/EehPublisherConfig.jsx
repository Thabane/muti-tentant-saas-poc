import WarehousePublisherConfig from './WarehousePublisherConfig';

function EehPublisherConfig({ publisher, onChange }) {
  const handleWarehousePublisherChange = (updatedWarehousePublisher) => {
    onChange({
      ...publisher,
      warehousePublisher: updatedWarehousePublisher,
    });
  };

  return (
    <div>
      <WarehousePublisherConfig
        warehousePublisher={publisher?.warehousePublisher}
        onChange={handleWarehousePublisherChange}
      />
    </div>
  );
}

export default EehPublisherConfig;
